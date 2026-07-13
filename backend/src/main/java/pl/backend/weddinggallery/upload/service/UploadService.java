package pl.backend.weddinggallery.upload.service;

import java.time.*;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
public class UploadService {
	private final UploadSessionRepository sessionRepository;
	private final MediaFileRepository mediaRepository;
	private final GalleryRepository galleryRepository;
	private final GalleryAccessRepository accessRepository;
	private final GalleryAccessService accessService;
	private final UploadFileValidator fileValidator;
	private final StorageService storageService;
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

	public CreateResult create(String slug, String idempotencyKey, UploadManifestRequest request,
			jakarta.servlet.http.HttpSession httpSession) {
		GalleryAccessService.GrantedGallery grant = accessService.requireGrant(slug, httpSession, true);
		if (idempotencyKey == null || !idempotencyKey.matches("[A-Za-z0-9._:-]{8,128}"))
			throw new AppException(UploadErrorCode.UPLOAD_INVALID_IDEMPOTENCY_KEY);
		validateManifest(request);
		String fingerprint = fingerprint(request);
		String grantFingerprint = tokenService.hash(grant.httpSessionId());
		return tx(() -> {
			Optional<UploadSession> existing = sessionRepository.findByPublicAccessIdAndIdempotencyKey(grant.accessId(),
					idempotencyKey);
			if (existing.isPresent()) {
				if (!existing.get().getRequestFingerprint().equals(fingerprint)
						|| !existing.get().getGrantFingerprint().equals(grantFingerprint))
					throw new AppException(UploadErrorCode.IDEMPOTENCY_KEY_CONFLICT);
				return new CreateResult(response(existing.get()), false);
			}
			if (sessionRepository.countByPublicAccessIdAndStatusAndExpiresAtAfter(grant.accessId(),
					UploadSessionStatus.OPEN, LocalDateTime.now()) >= maxActiveSessions)
				throw new AppException(UploadErrorCode.UPLOAD_SESSION_LIMIT_EXCEEDED);
			Gallery gallery = galleryRepository.findWithLockById(grant.gallery().getId())
					.orElseThrow(() -> new AppException(UploadErrorCode.UPLOAD_SESSION_NOT_FOUND));
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

	public UploadSessionResponse cancel(String slug, String sessionId, jakarta.servlet.http.HttpSession httpSession) {
		GalleryAccessService.GrantedGallery grant = accessService.requireGrant(slug, httpSession, true);
		return tx(() -> {
			UploadSession session = requireSession(sessionId, grant);
			if (session.getStatus() == UploadSessionStatus.CANCELLED)
				return response(session);
			if (session.getFiles().stream().anyMatch(file -> file.getStatus() == MediaStatus.RECEIVING))
				throw new AppException(UploadErrorCode.UPLOAD_IN_PROGRESS);
			long release = session.getFiles().stream().filter(file -> file.getStatus() != MediaStatus.STORED)
					.mapToLong(MediaFile::getExpectedSizeBytes).sum();
			Gallery gallery = galleryRepository.findWithLockById(session.getGallery().getId()).orElseThrow();
			gallery.setStorageReservedBytes(Math.max(0, gallery.getStorageReservedBytes() - release));
			session.getFiles().stream().filter(file -> file.getStatus() != MediaStatus.STORED)
					.forEach(file -> file.setStatus(MediaStatus.CANCELLED));
			session.setReservedBytes(Math.max(0, session.getReservedBytes() - release));
			session.setStatus(UploadSessionStatus.CANCELLED);
			session.setCancelledAt(LocalDateTime.now());
			return response(session);
		});
	}

	private Claim claim(String sessionId, String clientFileId, GalleryAccessService.GrantedGallery grant) {
		UploadSession session = requireSession(sessionId, grant);
		if (session.getStatus() != UploadSessionStatus.OPEN)
			throw new AppException(UploadErrorCode.UPLOAD_SESSION_NOT_OPEN);
		MediaFile media = mediaRepository.findByUploadSessionIdAndClientFileId(sessionId, clientFileId)
				.orElseThrow(() -> new AppException(UploadErrorCode.UPLOAD_FILE_NOT_FOUND));
		if (media.getStatus() == MediaStatus.STORED)
			return Claim.completed(fileResponse(media));
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
		media.setStatus(MediaStatus.STORED);
		media.setDetectedContentType(detected.detectedContentType());
		media.setSizeBytes(stored.size());
		media.setChecksumSha256(stored.checksumSha256());
		media.setStoredAt(LocalDateTime.now());
		UploadSession session = media.getUploadSession();
		Gallery gallery = galleryRepository.findWithLockById(claim.galleryId()).orElseThrow();
		gallery.setStorageReservedBytes(Math.max(0, gallery.getStorageReservedBytes() - media.getExpectedSizeBytes()));
		gallery.setStorageUsedBytes(gallery.getStorageUsedBytes() + stored.size());
		session.setReservedBytes(Math.max(0, session.getReservedBytes() - media.getExpectedSizeBytes()));
		if (session.getFiles().stream().allMatch(item -> item.getStatus() == MediaStatus.STORED))
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
		if (!LocalDateTime.now().isBefore(session.getExpiresAt()) && session.getStatus() == UploadSessionStatus.OPEN)
			throw new AppException(UploadErrorCode.UPLOAD_SESSION_NOT_OPEN);
		return session;
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
