package pl.backend.weddinggallery.upload.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.*;
import org.springframework.web.multipart.MultipartFile;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.media.model.*;
import pl.backend.weddinggallery.media.repository.MediaFileRepository;
import pl.backend.weddinggallery.publicaccess.model.GalleryAccess;
import pl.backend.weddinggallery.publicaccess.repository.GalleryAccessRepository;
import pl.backend.weddinggallery.publicaccess.service.*;
import pl.backend.weddinggallery.storage.*;
import pl.backend.weddinggallery.upload.dto.UploadManifestRequest;
import pl.backend.weddinggallery.upload.dto.UploadManifestRequest.ManifestFile;
import pl.backend.weddinggallery.upload.exception.UploadErrorCode;
import pl.backend.weddinggallery.upload.model.*;
import pl.backend.weddinggallery.upload.repository.UploadSessionRepository;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {
	@Mock
	UploadSessionRepository sessions;
	@Mock
	MediaFileRepository media;
	@Mock
	GalleryRepository galleries;
	@Mock
	GalleryAccessRepository accesses;
	@Mock
	GalleryAccessService grants;
	@Mock
	UploadFileValidator validator;
	@Mock
	StorageService storage;
	@Mock
	TokenService tokens;
	@Mock
	AuditService audit;
	@Mock
	PlatformTransactionManager transactions;
	@Mock
	TransactionStatus transactionStatus;
	private UploadService service;
	private Gallery gallery;
	private GalleryAccess access;
	private GalleryAccessService.GrantedGallery grant;
	private MockHttpSession httpSession;

	@BeforeEach
	void setUp() {
		service = new UploadService(sessions, media, galleries, accesses, grants, validator, storage, tokens, audit,
				transactions);
		ReflectionTestUtils.setField(service, "maxSessionBytes", 100L);
		ReflectionTestUtils.setField(service, "maxActiveSessions", 3);
		ReflectionTestUtils.setField(service, "galleryQuotaBytes", 200L);
		ReflectionTestUtils.setField(service, "sessionTtlMinutes", 30L);
		lenient().when(transactions.getTransaction(any())).thenReturn(transactionStatus);
		gallery = Gallery.builder().id("gallery").event(Event.builder().id("event").build()).storageUsedBytes(10)
				.storageReservedBytes(20).build();
		access = GalleryAccess.builder().id("access").gallery(gallery).build();
		grant = new GalleryAccessService.GrantedGallery(gallery, "access", "http-session");
		httpSession = new MockHttpSession();
	}

	@Test
	void rejectsEveryIdempotencyAndManifestIdentityBoundaryBeforeReservation() {
		stubGrant();
		for (String key : new String[]{null, "short", "contains space", "a".repeat(129)})
			assertCode(() -> service.create("slug", key, manifest(file("a", "a.jpg", 1)), httpSession),
					UploadErrorCode.UPLOAD_INVALID_IDEMPOTENCY_KEY);

		when(validator.validateDeclaration(anyString(), anyString(), anyLong()))
				.thenReturn(new UploadFileValidator.DetectedFile(MediaType.IMAGE, "image/jpeg", 100));
		for (UploadManifestRequest invalid : List.of(manifest(file("same", "a.jpg", 1), file("same", "b.jpg", 1)),
				manifest(file("one", "folder/a.jpg", 1)), manifest(file("one", "folder\\a.jpg", 1)),
				manifest(file("one", "bad\nname.jpg", 1))))
			assertCode(() -> service.create("slug", "valid-key", invalid, httpSession),
					UploadErrorCode.UPLOAD_CONTENT_MISMATCH);

		assertCode(() -> service.create("slug", "valid-key", manifest(file("one", "a.jpg", 101)), httpSession),
				UploadErrorCode.UPLOAD_FILE_TOO_LARGE);
		assertCode(() -> service.create("slug", "valid-key",
				manifest(file("one", "a.jpg", Long.MAX_VALUE), file("two", "b.jpg", Long.MAX_VALUE)), httpSession),
				UploadErrorCode.UPLOAD_FILE_TOO_LARGE);
	}

	@Test
	void createSupportsSuccessAndExactReplayButRejectsChangedReplay() {
		stubCreateDependencies();
		UploadManifestRequest request = manifest(file("one", "a.jpg", 10));
		when(sessions.findByGrantFingerprintAndIdempotencyKey("grant-hash", "valid-key")).thenReturn(Optional.empty());
		when(sessions.countByGrantFingerprintAndStatusAndExpiresAtAfter(eq("grant-hash"), eq(UploadSessionStatus.OPEN),
				any())).thenReturn(0L);
		when(accesses.findById("access")).thenReturn(Optional.of(access));

		var created = service.create("slug", "valid-key", request, httpSession);
		assertThat(created.created()).isTrue();
		assertThat(created.response().files()).hasSize(1);
		assertThat(gallery.getStorageReservedBytes()).isEqualTo(30);
		ArgumentCaptor<UploadSession> saved = ArgumentCaptor.forClass(UploadSession.class);
		verify(sessions).save(saved.capture());

		when(sessions.findByGrantFingerprintAndIdempotencyKey("grant-hash", "valid-key"))
				.thenReturn(Optional.of(saved.getValue()));
		assertThat(service.create("slug", "valid-key", request, httpSession).created()).isFalse();
		saved.getValue().setRequestFingerprint("different");
		assertCode(() -> service.create("slug", "valid-key", request, httpSession),
				UploadErrorCode.IDEMPOTENCY_KEY_CONFLICT);
		saved.getValue().setRequestFingerprint("request-hash");
		saved.getValue().setGrantFingerprint("different");
		assertCode(() -> service.create("slug", "valid-key", request, httpSession),
				UploadErrorCode.IDEMPOTENCY_KEY_CONFLICT);
	}

	@Test
	void createRejectsActiveSessionLimitQuotaAndMissingLockedResources() {
		stubCreateDependencies();
		UploadManifestRequest request = manifest(file("one", "a.jpg", 10));
		when(sessions.findByGrantFingerprintAndIdempotencyKey(anyString(), anyString())).thenReturn(Optional.empty());
		when(sessions.countByGrantFingerprintAndStatusAndExpiresAtAfter(anyString(), any(), any())).thenReturn(3L);
		assertCode(() -> service.create("slug", "valid-key", request, httpSession),
				UploadErrorCode.UPLOAD_SESSION_LIMIT_EXCEEDED);

		when(sessions.countByGrantFingerprintAndStatusAndExpiresAtAfter(anyString(), any(), any())).thenReturn(0L);
		gallery.setStorageUsedBytes(191);
		assertCode(() -> service.create("slug", "valid-key", request, httpSession),
				UploadErrorCode.STORAGE_QUOTA_EXCEEDED);

		gallery.setStorageUsedBytes(0);
		gallery.setStorageReservedBytes(0);
		when(accesses.findById("access")).thenReturn(Optional.empty());
		assertCode(() -> service.create("slug", "valid-key", request, httpSession),
				UploadErrorCode.UPLOAD_SESSION_NOT_FOUND);

		when(galleries.findWithLockById("gallery")).thenReturn(Optional.empty());
		assertCode(() -> service.create("slug", "valid-key", request, httpSession),
				UploadErrorCode.UPLOAD_SESSION_NOT_FOUND);
	}

	@Test
	void preflightAndCancelCoverStoredOpenClosedReceivingAndExpiredStates() {
		stubGrant();
		UploadSession session = session(UploadSessionStatus.OPEN);
		MediaFile file = file(session, MediaStatus.PENDING, 10);
		stubSession(session);
		when(media.findByUploadSessionIdAndClientFileId("session", "file")).thenReturn(Optional.of(file));
		assertThat(service.preflightUpload("slug", "session", "file", httpSession)).isEqualTo(10);

		file.setStatus(MediaStatus.STORED);
		session.setStatus(UploadSessionStatus.COMPLETED);
		assertThat(service.preflightUpload("slug", "session", "file", httpSession)).isEqualTo(10);
		file.setStatus(MediaStatus.PENDING);
		assertCode(() -> service.preflightUpload("slug", "session", "file", httpSession),
				UploadErrorCode.UPLOAD_SESSION_NOT_OPEN);

		session.setStatus(UploadSessionStatus.CANCELLED);
		assertThat(service.cancel("slug", "session", httpSession).status()).isEqualTo(UploadSessionStatus.CANCELLED);
		session.setStatus(UploadSessionStatus.OPEN);
		file.setStatus(MediaStatus.RECEIVING);
		assertCode(() -> service.cancel("slug", "session", httpSession), UploadErrorCode.UPLOAD_IN_PROGRESS);

		file.setStatus(MediaStatus.PENDING);
		when(galleries.findWithLockById("gallery")).thenReturn(Optional.of(gallery));
		assertThat(service.cancel("slug", "session", httpSession).status()).isEqualTo(UploadSessionStatus.CANCELLED);
		assertThat(file.getStatus()).isEqualTo(MediaStatus.CANCELLED);
	}

	@Test
	void uploadReplaysStoredFilesAndRejectsConflictingLifecycleStates() {
		stubGrant();
		UploadSession session = session(UploadSessionStatus.OPEN);
		MediaFile file = file(session, MediaStatus.STORED, 10);
		file.setSizeBytes(9L);
		stubSession(session);
		when(media.findByUploadSessionIdAndClientFileId("session", "file")).thenReturn(Optional.of(file));
		assertThat(service.upload("slug", "session", "file", mock(MultipartFile.class), httpSession).size())
				.isEqualTo(9);

		file.setStatus(MediaStatus.RECEIVING);
		assertCode(() -> service.upload("slug", "session", "file", mock(MultipartFile.class), httpSession),
				UploadErrorCode.UPLOAD_IN_PROGRESS);
		file.setStatus(MediaStatus.CANCELLED);
		assertCode(() -> service.upload("slug", "session", "file", mock(MultipartFile.class), httpSession),
				UploadErrorCode.UPLOAD_SESSION_NOT_OPEN);
		session.setStatus(UploadSessionStatus.EXPIRED);
		file.setStatus(MediaStatus.PENDING);
		assertCode(() -> service.upload("slug", "session", "file", mock(MultipartFile.class), httpSession),
				UploadErrorCode.UPLOAD_SESSION_NOT_OPEN);
	}

	@Test
	void uploadFinalizesExactObjectsAndCompensatesSizeStorageAndCleanupFailures() throws Exception {
		stubGrant();
		UploadSession session = session(UploadSessionStatus.OPEN);
		MediaFile file = file(session, MediaStatus.PENDING, 10);
		stubSession(session);
		when(media.findByUploadSessionIdAndClientFileId("session", "file")).thenReturn(Optional.of(file));
		when(media.findById("media")).thenReturn(Optional.of(file));
		when(galleries.findWithLockById("gallery")).thenReturn(Optional.of(gallery));
		var detected = new UploadFileValidator.DetectedFile(MediaType.IMAGE, "image/jpeg", 100);
		MultipartFile multipart = mock(MultipartFile.class);
		when(validator.validate("a.jpg", "image/jpeg", 10, multipart)).thenReturn(detected);
		when(multipart.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(new byte[10]));
		when(storage.save(eq("object-key"), any(), eq(100L))).thenReturn(new StoredObject(10, "checksum"));

		assertThat(service.upload("slug", "session", "file", multipart, httpSession).status())
				.isEqualTo(MediaStatus.STORED);
		assertThat(session.getStatus()).isEqualTo(UploadSessionStatus.COMPLETED);

		file.setStatus(MediaStatus.PENDING);
		session.setStatus(UploadSessionStatus.OPEN);
		when(storage.save(eq("object-key"), any(), eq(100L))).thenReturn(new StoredObject(9, "short"));
		assertCode(() -> service.upload("slug", "session", "file", multipart, httpSession),
				UploadErrorCode.UPLOAD_SIZE_MISMATCH);
		assertThat(file.getStatus()).isEqualTo(MediaStatus.FAILED);
		verify(storage).delete("object-key");

		file.setStatus(MediaStatus.PENDING);
		doThrow(new RuntimeException("cleanup failed")).when(storage).delete("object-key");
		assertCode(() -> service.upload("slug", "session", "file", multipart, httpSession),
				UploadErrorCode.UPLOAD_SIZE_MISMATCH);
		assertThat(file.getStatus()).isEqualTo(MediaStatus.CLEANUP_REQUIRED);

		file.setStatus(MediaStatus.PENDING);
		when(multipart.getInputStream()).thenThrow(new java.io.IOException("source failed"));
		assertCode(() -> service.upload("slug", "session", "file", multipart, httpSession),
				UploadErrorCode.STORAGE_WRITE_FAILED);
	}

	@Test
	void expiryAndReservationReleaseCoverEveryFileLifecycleAndCleanupOutcome() {
		stubGrant();
		UploadSession expiredOpen = session(UploadSessionStatus.OPEN);
		expiredOpen.setExpiresAt(LocalDateTime.now().minusSeconds(1));
		MediaFile stored = file(expiredOpen, MediaStatus.STORED, 1);
		MediaFile cleanup = file(expiredOpen, MediaStatus.CLEANUP_REQUIRED, 2);
		MediaFile receivingMissing = file(expiredOpen, MediaStatus.RECEIVING, 3);
		receivingMissing.setStorageKey("missing");
		MediaFile receivingPresent = file(expiredOpen, MediaStatus.RECEIVING, 4);
		receivingPresent.setStorageKey("present");
		when(tokens.hash("http-session")).thenReturn("grant-hash");
		when(sessions.findByIdAndGalleryIdAndPublicAccessIdAndGrantFingerprint(anyString(), anyString(), anyString(),
				anyString())).thenReturn(Optional.of(expiredOpen));
		when(galleries.findWithLockById("gallery")).thenReturn(Optional.of(gallery));
		when(storage.exists("missing")).thenReturn(false);
		when(storage.exists("present")).thenReturn(true);

		assertCode(() -> service.get("slug", "session", httpSession), UploadErrorCode.UPLOAD_SESSION_NOT_OPEN);
		assertThat(stored.getStatus()).isEqualTo(MediaStatus.STORED);
		assertThat(cleanup.getStatus()).isEqualTo(MediaStatus.CLEANUP_REQUIRED);
		assertThat(receivingMissing.getStatus()).isEqualTo(MediaStatus.CANCELLED);
		assertThat(receivingPresent.getStatus()).isEqualTo(MediaStatus.CANCELLED);
		verify(storage).delete("present");

		UploadSession expiredCompleted = session(UploadSessionStatus.COMPLETED);
		expiredCompleted.setExpiresAt(LocalDateTime.now().minusSeconds(1));
		when(sessions.findByIdAndGalleryIdAndPublicAccessIdAndGrantFingerprint(anyString(), anyString(), anyString(),
				anyString())).thenReturn(Optional.of(expiredCompleted));
		assertThat(service.get("slug", "session", httpSession).status()).isEqualTo(UploadSessionStatus.COMPLETED);
	}

	@Test
	void scheduledRecoveryDeletesOrMarksObjectsAndReleasesReservationsSafely() {
		UploadSession open = session(UploadSessionStatus.OPEN);
		MediaFile openReceiving = file(open, MediaStatus.RECEIVING, 10);
		UploadSession expired = session(UploadSessionStatus.EXPIRED);
		MediaFile expiredReceiving = file(expired, MediaStatus.RECEIVING, 10);
		when(media.findByStatusAndUpdatedAtBefore(eq(MediaStatus.RECEIVING), any())).thenReturn(List.of(openReceiving));
		when(media.findByStatusAndUpdatedAtBefore(eq(MediaStatus.CLEANUP_REQUIRED), any()))
				.thenReturn(List.of(expiredReceiving));
		when(storage.exists(anyString())).thenReturn(true);
		when(galleries.findWithLockById("gallery")).thenReturn(Optional.of(gallery));
		when(sessions.findAllReleasableOpenSessions(any())).thenReturn(List.of());

		service.releaseExpiredAndRevokedSessions();

		verify(storage, times(2)).delete(anyString());
		assertThat(openReceiving.getStatus()).isEqualTo(MediaStatus.FAILED);
		assertThat(expiredReceiving.getStatus()).isEqualTo(MediaStatus.CANCELLED);
	}

	private void stubGrant() {
		when(grants.requireGrant("slug", httpSession, true)).thenReturn(grant);
	}

	private void stubCreateDependencies() {
		stubGrant();
		when(tokens.hash("http-session")).thenReturn("grant-hash");
		when(tokens.hash(contains("one|a.jpg"))).thenReturn("request-hash");
		when(galleries.findWithLockById("gallery")).thenReturn(Optional.of(gallery));
		when(sessions.findReleasableOpenSessions(eq("gallery"), any())).thenReturn(List.of());
		when(validator.validateDeclaration("a.jpg", "image/jpeg", 10))
				.thenReturn(new UploadFileValidator.DetectedFile(MediaType.IMAGE, "image/jpeg", 100));
	}

	private UploadSession session(UploadSessionStatus status) {
		return UploadSession.builder().id("session").gallery(gallery).publicAccess(access)
				.grantFingerprint("grant-hash").status(status).expiresAt(LocalDateTime.now().plusMinutes(10))
				.reservedBytes(10).build();
	}

	private MediaFile file(UploadSession session, MediaStatus status, long size) {
		MediaFile file = MediaFile.builder().id("media").uploadSession(session).gallery(gallery).clientFileId("file")
				.originalFilename("a.jpg").declaredContentType("image/jpeg").storageKey("object-key")
				.expectedSizeBytes(size).status(status).build();
		session.getFiles().add(file);
		return file;
	}

	private void stubSession(UploadSession session) {
		when(tokens.hash("http-session")).thenReturn("grant-hash");
		when(sessions.findByIdAndGalleryIdAndPublicAccessIdAndGrantFingerprint("session", "gallery", "access",
				"grant-hash")).thenReturn(Optional.of(session));
	}

	private ManifestFile file(String id, String name, long size) {
		return new ManifestFile(id, name, "image/jpeg", size);
	}

	private UploadManifestRequest manifest(ManifestFile... files) {
		return new UploadManifestRequest(List.of(files));
	}

	private void assertCode(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, UploadErrorCode code) {
		assertThatThrownBy(call).isInstanceOfSatisfying(AppException.class,
				error -> assertThat(error.getErrorCode()).isEqualTo(code));
	}
}
