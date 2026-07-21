package pl.backend.weddinggallery.upload.service;

import java.time.*;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.media.model.*;
import pl.backend.weddinggallery.media.repository.MediaFileRepository;
import pl.backend.weddinggallery.media.service.MediaProcessingJobService;
import pl.backend.weddinggallery.publicaccess.model.GalleryAccess;
import pl.backend.weddinggallery.publicaccess.repository.GalleryAccessRepository;
import pl.backend.weddinggallery.publicaccess.service.GalleryAccessService;
import pl.backend.weddinggallery.publicaccess.service.TokenService;
import pl.backend.weddinggallery.storage.*;
import pl.backend.weddinggallery.upload.dto.*;
import pl.backend.weddinggallery.upload.exception.UploadErrorCode;
import pl.backend.weddinggallery.upload.model.*;
import pl.backend.weddinggallery.upload.repository.UploadSessionRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {
	private final UploadSessionRepository sessionRepository;
	private final MediaFileRepository mediaRepository;
	private final GalleryRepository galleryRepository;
	private final GalleryAccessRepository accessRepository;
	private final GalleryAccessService accessService;
	private final UploadFileValidator fileValidator;
	private final StorageService storageService;
	private final MediaProcessingJobService processingJobs;
	private final TokenService tokenService;
	private final AuditService auditService;
	private final PlatformTransactionManager transactionManager;
	@Value("${app.upload.max-session-bytes:2147483648}")
	private long maxSessionBytes;
	@Value("${app.upload.max-active-sessions:3}")
	private int maxActiveSessions;
	@Value("${app.upload.gallery-quota-bytes:5368709120}")
	private long galleryQuotaBytes;
	@Value("${app.upload.session-ttl-minutes:30}")
	private long sessionTtlMinutes;
	private final LocalDateTime processStartedAt = LocalDateTime.now();

	public CreateResult create(String slug, String idempotencyKey, UploadManifestRequest request,
			jakarta.servlet.http.HttpSession httpSession) {
		GalleryAccessService.GrantedGallery grant = accessService.requireGrant(slug, httpSession, true);
		if (idempotencyKey == null || !idempotencyKey.matches("[A-Za-z0-9._:-]{8,128}"))
			throw new AppException(UploadErrorCode.UPLOAD_INVALID_IDEMPOTENCY_KEY);
		validateManifest(request);
		String fingerprint = fingerprint(request);
		String grantFingerprint = tokenService.hash(grant.httpSessionId());
		return tx(() -> {
			Gallery gallery = galleryRepository.findWithLockById(grant.gallery().getId())
					.orElseThrow(() -> new AppException(UploadErrorCode.UPLOAD_SESSION_NOT_FOUND));
			releaseStaleReservations(gallery, LocalDateTime.now());
			Optional<UploadSession> existing = sessionRepository
					.findByGrantFingerprintAndIdempotencyKey(grantFingerprint, idempotencyKey);
			if (existing.isPresent()) {
				if (!existing.get().getRequestFingerprint().equals(fingerprint)
						|| !existing.get().getGrantFingerprint().equals(grantFingerprint))
					throw new AppException(UploadErrorCode.IDEMPOTENCY_KEY_CONFLICT);
				return new CreateResult(response(existing.get()), false);
			}
			if (sessionRepository.countByGrantFingerprintAndStatusAndExpiresAtAfter(grantFingerprint,
					UploadSessionStatus.OPEN, LocalDateTime.now()) >= maxActiveSessions)
				throw new AppException(UploadErrorCode.UPLOAD_SESSION_LIMIT_EXCEEDED);
			long total = totalBytes(request);
			if (gallery.getStorageUsedBytes() + gallery.getStorageReservedBytes() + total > galleryQuotaBytes)
				throw new AppException(UploadErrorCode.STORAGE_QUOTA_EXCEEDED);
			GalleryAccess access = accessRepository.findById(grant.accessId())
					.orElseThrow(() -> new AppException(UploadErrorCode.UPLOAD_SESSION_NOT_FOUND));
			String sessionId = UUID.randomUUID().toString();
			UploadSession upload = UploadSession.builder().id(sessionId).gallery(gallery).publicAccess(access)
					.grantFingerprint(grantFingerprint).idempotencyKey(idempotencyKey).requestFingerprint(fingerprint)
					.status(UploadSessionStatus.OPEN).totalFiles(request.files().size()).totalBytes(total)
					.reservedBytes(total).expiresAt(LocalDateTime.now().plusMinutes(sessionTtlMinutes)).build();
			for (UploadManifestRequest.ManifestFile item : request.files()) {
				UploadFileValidator.DetectedFile detected = fileValidator.validateDeclaration(item.fileName(),
						item.declaredContentType(), item.size());
				String mediaId = UUID.randomUUID().toString();
				MediaFile media = MediaFile.builder().id(mediaId).uploadSession(upload).gallery(gallery)
						.clientFileId(item.clientFileId()).originalFilename(item.fileName())
						.storageKey("events/" + gallery.getEvent().getId() + "/galleries/" + gallery.getId() + "/media/"
								+ mediaId + "/original")
						.expectedSizeBytes(item.size()).declaredContentType(item.declaredContentType())
						.mediaType(detected.mediaType()).status(MediaStatus.PENDING).build();
				upload.getFiles().add(media);
			}
			gallery.setStorageReservedBytes(gallery.getStorageReservedBytes() + total);
			sessionRepository.save(upload);
			log.info("Upload session created sessionId={} galleryId={} files={} totalBytes={}", sessionId,
					gallery.getId(), request.files().size(), total);
			return new CreateResult(response(upload), true);
		});
	}

	public UploadSessionResponse get(String slug, String sessionId, jakarta.servlet.http.HttpSession httpSession) {
		GalleryAccessService.GrantedGallery grant = accessService.requireGrant(slug, httpSession, true);
		return tx(() -> response(requireSession(sessionId, grant)));
	}

	public UploadFileResponse upload(String slug, String sessionId, String clientFileId, MultipartFile file,
			jakarta.servlet.http.HttpSession httpSession) {
		GalleryAccessService.GrantedGallery grant = accessService.requireGrant(slug, httpSession, true);
		Claim claim = tx(() -> claim(sessionId, clientFileId, grant));
		if (claim.completed() != null)
			return claim.completed();
		boolean objectWritten = false;
		try {
			UploadFileValidator.DetectedFile detected = fileValidator.validate(claim.fileName(),
					claim.declaredContentType(), claim.expectedSize(), file);
			StoredObject stored = storageService.save(claim.storageKey(), file.getInputStream(), detected.maxBytes());
			objectWritten = true;
			if (stored.size() != claim.expectedSize())
				throw new AppException(UploadErrorCode.UPLOAD_SIZE_MISMATCH);
			return tx(() -> finalizeStored(claim, detected, stored));
		} catch (AppException ex) {
			compensate(claim, objectWritten, ex.getErrorCode().name());
			throw ex;
		} catch (Exception ex) {
			compensate(claim, objectWritten, UploadErrorCode.STORAGE_WRITE_FAILED.name());
			throw new AppException(UploadErrorCode.STORAGE_WRITE_FAILED);
		}
	}

	public long preflightUpload(String slug, String sessionId, String clientFileId,
			jakarta.servlet.http.HttpSession httpSession) {
		GalleryAccessService.GrantedGallery grant = accessService.requireGrant(slug, httpSession, true);
		return tx(() -> {
			UploadSession session = requireSession(sessionId, grant);
			MediaFile media = mediaRepository.findByUploadSessionIdAndClientFileId(sessionId, clientFileId)
					.orElseThrow(() -> new AppException(UploadErrorCode.UPLOAD_FILE_NOT_FOUND));
			if (!isStoredOrProcessing(media.getStatus()) && session.getStatus() != UploadSessionStatus.OPEN)
				throw new AppException(UploadErrorCode.UPLOAD_SESSION_NOT_OPEN);
			return media.getExpectedSizeBytes();
		});
	}

	public UploadSessionResponse cancel(String slug, String sessionId, jakarta.servlet.http.HttpSession httpSession) {
		GalleryAccessService.GrantedGallery grant = accessService.requireGrant(slug, httpSession, true);
		return tx(() -> {
			UploadSession session = requireSession(sessionId, grant);
			if (session.getStatus() == UploadSessionStatus.CANCELLED)
				return response(session);
			if (session.getFiles().stream().anyMatch(file -> file.getStatus() == MediaStatus.RECEIVING))
				throw new AppException(UploadErrorCode.UPLOAD_IN_PROGRESS);
			releaseSessionReservation(session, UploadSessionStatus.CANCELLED);
			session.setCancelledAt(LocalDateTime.now());
			return response(session);
		});
	}

	@Scheduled(fixedDelayString = "${app.upload.cleanup-interval-ms:300000}", initialDelayString = "${app.upload.cleanup-initial-delay-ms:60000}")
	public void releaseExpiredAndRevokedSessions() {
		tx(() -> {
			for (MediaFile media : mediaRepository.findByStatusAndUpdatedAtBefore(MediaStatus.RECEIVING,
					processStartedAt))
				reconcileObject(media);
			for (MediaFile media : mediaRepository.findByStatusAndUpdatedAtBefore(MediaStatus.CLEANUP_REQUIRED,
					LocalDateTime.now()))
				reconcileObject(media);
			for (UploadSession session : sessionRepository.findAllReleasableOpenSessions(LocalDateTime.now()))
				releaseSessionReservation(session, UploadSessionStatus.EXPIRED);
			return null;
		});
	}

	private void reconcileObject(MediaFile media) {
		try {
			if (storageService.exists(media.getStorageKey()))
				storageService.delete(media.getStorageKey());
			if (media.getUploadSession().getStatus() == UploadSessionStatus.OPEN) {
				media.setStatus(MediaStatus.FAILED);
			} else {
				Gallery gallery = galleryRepository.findWithLockById(media.getGallery().getId()).orElseThrow();
				gallery.setStorageReservedBytes(
						Math.max(0, gallery.getStorageReservedBytes() - media.getExpectedSizeBytes()));
				media.getUploadSession().setReservedBytes(
						Math.max(0, media.getUploadSession().getReservedBytes() - media.getExpectedSizeBytes()));
				media.setStatus(MediaStatus.CANCELLED);
			}
			media.setFailureCode("INTERRUPTED_UPLOAD");
		} catch (RuntimeException ex) {
			media.setStatus(MediaStatus.CLEANUP_REQUIRED);
			media.setFailureCode("INTERRUPTED_UPLOAD_CLEANUP_FAILED");
			log.warn("Interrupted upload cleanup failed mediaId={} galleryId={}", media.getId(),
					media.getGallery().getId(), ex);
		}
	}

	private Claim claim(String sessionId, String clientFileId, GalleryAccessService.GrantedGallery grant) {
		UploadSession session = requireSession(sessionId, grant);
		MediaFile media = mediaRepository.findByUploadSessionIdAndClientFileId(sessionId, clientFileId)
				.orElseThrow(() -> new AppException(UploadErrorCode.UPLOAD_FILE_NOT_FOUND));
		if (isStoredOrProcessing(media.getStatus()))
			return Claim.completed(fileResponse(media));
		if (session.getStatus() != UploadSessionStatus.OPEN)
			throw new AppException(UploadErrorCode.UPLOAD_SESSION_NOT_OPEN);
		if (media.getStatus() == MediaStatus.RECEIVING)
			throw new AppException(UploadErrorCode.UPLOAD_IN_PROGRESS);
		if (media.getStatus() == MediaStatus.CANCELLED)
			throw new AppException(UploadErrorCode.UPLOAD_SESSION_NOT_OPEN);
		media.setStatus(MediaStatus.RECEIVING);
		media.setFailureCode(null);
		return new Claim(media.getId(), sessionId, media.getGallery().getId(), media.getGallery().getEvent().getId(),
				grant.accessId(), media.getClientFileId(), media.getOriginalFilename(), media.getDeclaredContentType(),
				media.getExpectedSizeBytes(), media.getStorageKey(), null);
	}

	private UploadFileResponse finalizeStored(Claim claim, UploadFileValidator.DetectedFile detected,
			StoredObject stored) {
		MediaFile media = mediaRepository.findById(claim.mediaId()).orElseThrow();
		if (media.getStatus() != MediaStatus.RECEIVING)
			throw new AppException(UploadErrorCode.UPLOAD_IN_PROGRESS);
		media.setStatus(MediaStatus.PROCESSING);
		media.setDetectedContentType(detected.detectedContentType());
		media.setSizeBytes(stored.size());
		media.setChecksumSha256(stored.checksumSha256());
		media.setStoredAt(LocalDateTime.now());
		processingJobs.enqueue(media);
		log.info("Media original stored mediaId={} galleryId={} sessionId={} size={} status={}", media.getId(),
				claim.galleryId(), claim.sessionId(), stored.size(), media.getStatus());
		UploadSession session = media.getUploadSession();
		Gallery gallery = galleryRepository.findWithLockById(claim.galleryId()).orElseThrow();
		gallery.setStorageReservedBytes(Math.max(0, gallery.getStorageReservedBytes() - media.getExpectedSizeBytes()));
		gallery.setStorageUsedBytes(gallery.getStorageUsedBytes() + stored.size());
		session.setReservedBytes(Math.max(0, session.getReservedBytes() - media.getExpectedSizeBytes()));
		if (session.getFiles().stream().allMatch(item -> isStoredOrProcessing(item.getStatus())))
			session.setStatus(UploadSessionStatus.COMPLETED);
		auditService.logRequiredGuestGalleryEvent(claim.accessId(), EventType.MEDIA_STORED, claim.eventId(),
				claim.galleryId(), "mediaId=" + media.getId() + ",size=" + stored.size());
		return fileResponse(media);
	}

	private void compensate(Claim claim, boolean objectWritten, String failureCode) {
		boolean cleanupFailed = false;
		if (objectWritten) {
			try {
				storageService.delete(claim.storageKey());
			} catch (Exception ex) {
				cleanupFailed = true;
				log.warn("Upload compensation cleanup failed mediaId={} galleryId={} failureCode={}", claim.mediaId(),
						claim.galleryId(), failureCode, ex);
			}
		}
		boolean finalCleanupFailed = cleanupFailed;
		tx(() -> {
			mediaRepository.findById(claim.mediaId()).ifPresent(media -> {
				media.setStatus(finalCleanupFailed ? MediaStatus.CLEANUP_REQUIRED : MediaStatus.FAILED);
				media.setFailureCode(failureCode);
			});
			return null;
		});
	}

	private UploadSession requireSession(String id, GalleryAccessService.GrantedGallery grant) {
		UploadSession session = sessionRepository
				.findByIdAndGalleryIdAndPublicAccessIdAndGrantFingerprint(id, grant.gallery().getId(), grant.accessId(),
						tokenService.hash(grant.httpSessionId()))
				.orElseThrow(() -> new AppException(UploadErrorCode.UPLOAD_SESSION_NOT_FOUND));
		if (!LocalDateTime.now().isBefore(session.getExpiresAt()) && session.getStatus() == UploadSessionStatus.OPEN) {
			releaseSessionReservation(session, UploadSessionStatus.EXPIRED);
			throw new AppException(UploadErrorCode.UPLOAD_SESSION_NOT_OPEN);
		}
		return session;
	}

	private void releaseStaleReservations(Gallery gallery, LocalDateTime now) {
		for (UploadSession session : sessionRepository.findReleasableOpenSessions(gallery.getId(), now))
			releaseSessionReservation(session, UploadSessionStatus.EXPIRED);
	}

	private void releaseSessionReservation(UploadSession session, UploadSessionStatus targetStatus) {
		long release = 0;
		for (MediaFile file : session.getFiles()) {
			if (isStoredOrProcessing(file.getStatus()) || file.getStatus() == MediaStatus.CLEANUP_REQUIRED)
				continue;
			if (file.getStatus() == MediaStatus.RECEIVING) {
				try {
					if (storageService.exists(file.getStorageKey()))
						storageService.delete(file.getStorageKey());
				} catch (RuntimeException ex) {
					file.setStatus(MediaStatus.CLEANUP_REQUIRED);
					file.setFailureCode("EXPIRED_UPLOAD_CLEANUP_FAILED");
					continue;
				}
			}
			file.setStatus(MediaStatus.CANCELLED);
			release += file.getExpectedSizeBytes();
		}
		Gallery gallery = galleryRepository.findWithLockById(session.getGallery().getId()).orElseThrow();
		gallery.setStorageReservedBytes(Math.max(0, gallery.getStorageReservedBytes() - release));
		session.setReservedBytes(Math.max(0, session.getReservedBytes() - release));
		session.setStatus(targetStatus);
	}

	private void validateManifest(UploadManifestRequest request) {
		Set<String> ids = new HashSet<>();
		for (var file : request.files()) {
			if (!ids.add(file.clientFileId()) || file.fileName().contains("/") || file.fileName().contains("\\")
					|| file.fileName().chars().anyMatch(ch -> Character.isISOControl(ch)))
				throw new AppException(UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
			fileValidator.validateDeclaration(file.fileName(), file.declaredContentType(), file.size());
		}
		if (totalBytes(request) > maxSessionBytes)
			throw new AppException(UploadErrorCode.UPLOAD_FILE_TOO_LARGE);
	}
	private long totalBytes(UploadManifestRequest request) {
		try {
			long total = 0;
			for (var f : request.files())
				total = Math.addExact(total, f.size());
			return total;
		} catch (ArithmeticException ex) {
			throw new AppException(UploadErrorCode.UPLOAD_FILE_TOO_LARGE);
		}
	}
	private String fingerprint(UploadManifestRequest request) {
		StringBuilder value = new StringBuilder();
		for (var f : request.files())
			value.append(f.clientFileId()).append('|').append(f.fileName()).append('|').append(f.declaredContentType())
					.append('|').append(f.size()).append('\n');
		return tokenService.hash(value.toString());
	}
	private UploadSessionResponse response(UploadSession session) {
		return new UploadSessionResponse(session.getId(), session.getStatus(),
				session.getExpiresAt().toInstant(ZoneOffset.UTC),
				session.getFiles().stream().map(this::fileResponse).toList());
	}
	private UploadFileResponse fileResponse(MediaFile file) {
		return new UploadFileResponse(file.getClientFileId(), file.getOriginalFilename(),
				file.getSizeBytes() == null ? file.getExpectedSizeBytes() : file.getSizeBytes(), file.getStatus(),
				file.getDetectedContentType(), file.getChecksumSha256(), file.getFailureCode());
	}
	private boolean isStoredOrProcessing(MediaStatus status) {
		return status == MediaStatus.STORED || status == MediaStatus.PROCESSING || status == MediaStatus.PROCESSED
				|| status == MediaStatus.PROCESSING_FAILED;
	}
	private <T> T tx(Supplier<T> work) {
		return new TransactionTemplate(transactionManager).execute(status -> work.get());
	}
	public record CreateResult(UploadSessionResponse response, boolean created) {
	}
	private record Claim(String mediaId, String sessionId, String galleryId, String eventId, String accessId,
			String clientFileId, String fileName, String declaredContentType, long expectedSize, String storageKey,
			UploadFileResponse completed) {
		static Claim completed(UploadFileResponse response) {
			return new Claim(null, null, null, null, null, null, null, null, 0, null, response);
		}
	}
}
