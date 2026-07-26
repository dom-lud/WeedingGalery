package pl.backend.weddinggallery.media.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.exception.EventErrorCode;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.event.model.EventStatus;
import pl.backend.weddinggallery.event.service.EventService;
import pl.backend.weddinggallery.gallery.exception.GalleryErrorCode;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.model.GalleryStatus;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.media.model.MediaFile;
import pl.backend.weddinggallery.media.model.MediaStatus;
import pl.backend.weddinggallery.media.model.MediaThumbnail;
import pl.backend.weddinggallery.media.model.MediaThumbnailVariant;
import pl.backend.weddinggallery.media.model.MediaType;
import pl.backend.weddinggallery.media.repository.MediaFileRepository;
import pl.backend.weddinggallery.media.repository.MediaThumbnailRepository;
import pl.backend.weddinggallery.membership.model.EventRole;
import pl.backend.weddinggallery.publicaccess.service.GalleryAccessService;
import pl.backend.weddinggallery.storage.StorageService;
import pl.backend.weddinggallery.user.model.User;

@ExtendWith(MockitoExtension.class)
class MediaGalleryServiceTest {
	@Mock
	private MediaFileRepository mediaFiles;
	@Mock
	private MediaThumbnailRepository thumbnails;
	@Mock
	private GalleryRepository galleries;
	@Mock
	private GalleryAccessService galleryAccess;
	@Mock
	private EventService eventService;
	@Mock
	private StorageService storage;
	private MediaGalleryService service;
	private User owner;
	private Event event;
	private Gallery gallery;

	@BeforeEach
	void setUp() {
		service = new MediaGalleryService(mediaFiles, thumbnails, galleries, galleryAccess, eventService, storage);
		owner = User.builder().id("user-1").email("owner@example.com").build();
		event = Event.builder().id("event-1").owner(owner).status(EventStatus.DRAFT).build();
		gallery = Gallery.builder().id("gallery-1").event(event).slug("wedding").name("Wedding Gallery")
				.status(GalleryStatus.ACTIVE).build();
	}

	@Test
	void publicMediaShowsOnlyStoredExistingObjectsAndFallsBackToOriginalWhenThumbnailIsMissing() {
		HttpSession session = new MockHttpSession();
		when(galleryAccess.requireGrant("wedding", session, false))
				.thenReturn(new GalleryAccessService.GrantedGallery(gallery, "access-1", session.getId()));
		MediaFile visible = image("12345678-visible", "first.jpg", MediaStatus.PROCESSED, "image/jpeg");
		visible.setSizeBytes(null);
		MediaFile missingObject = image("12345678-missing", "missing.jpg", MediaStatus.PROCESSED, "image/jpeg");
		MediaFile pending = image("12345678-pending", "pending.jpg", MediaStatus.PENDING, "image/jpeg");
		when(mediaFiles.findByGalleryIdAndStatusInOrderByStoredAtDescCreatedAtDescIdAsc(eq(gallery.getId()), any()))
				.thenReturn(List.of(visible, missingObject, pending));
		when(storage.exists(visible.getStorageKey())).thenReturn(true);
		when(storage.exists(missingObject.getStorageKey())).thenReturn(false);
		when(thumbnails.findByMediaFileIdInAndVariant(List.of(visible.getId()), MediaThumbnailVariant.SMALL))
				.thenReturn(List.of());

		var response = service.publicMedia("wedding", session);

		assertThat(response).singleElement().satisfies(item -> {
			assertThat(item.id()).isEqualTo(visible.getId());
			assertThat(item.thumbnailUrl())
					.isEqualTo("/api/public/galleries/wedding/media/" + visible.getId() + "/content");
			assertThat(item.contentUrl())
					.isEqualTo("/api/public/galleries/wedding/media/" + visible.getId() + "/content");
			assertThat(item.size()).isEqualTo(visible.getExpectedSizeBytes());
			assertThat(item.uploadedAt()).isEqualTo("2026-07-23T18:00:00Z");
		});
	}

	@Test
	void publicMediaCapsResultsAt500() {
		HttpSession session = new MockHttpSession();
		when(galleryAccess.requireGrant("wedding", session, false))
				.thenReturn(new GalleryAccessService.GrantedGallery(gallery, "access-1", session.getId()));
		List<MediaFile> files = IntStream.range(0, 501).mapToObj(
				index -> image(String.format("%08d-photo", index), "photo.jpg", MediaStatus.PROCESSED, "image/jpeg"))
				.toList();
		when(mediaFiles.findByGalleryIdAndStatusInOrderByStoredAtDescCreatedAtDescIdAsc(eq(gallery.getId()), any()))
				.thenReturn(files);
		when(storage.exists(anyString())).thenReturn(true);
		when(thumbnails.findByMediaFileIdInAndVariant(anyList(), eq(MediaThumbnailVariant.SMALL)))
				.thenReturn(List.of());

		assertThat(service.publicMedia("wedding", session)).hasSize(500);
	}

	@Test
	void publicMediaExcludesProcessingFailuresAndAppliesAProviderIndependentLimit() {
		HttpSession session = new MockHttpSession();
		when(galleryAccess.requireGrant("wedding", session, false))
				.thenReturn(new GalleryAccessService.GrantedGallery(gallery, "access-1", session.getId()));
		MediaFile failed = image("12345678-failed", "failed.jpg", MediaStatus.PROCESSING_FAILED, "image/jpeg");
		MediaFile first = image("12345678-first", "first.jpg", MediaStatus.PROCESSED, "image/jpeg");
		when(mediaFiles.findByGalleryIdAndStatusInOrderByStoredAtDescCreatedAtDescIdAsc(eq(gallery.getId()),
				argThat(statuses -> statuses.equals(
						List.of(MediaStatus.STORED, MediaStatus.PROCESSING, MediaStatus.PROCESSED)))))
				.thenReturn(
						java.util.stream.Stream
								.concat(java.util.stream.Stream.of(failed),
										java.util.stream.IntStream.range(0, 501)
												.mapToObj(
														i -> i == 0
																? first
																: image("12345678-" + i, "photo-" + i + ".jpg",
																		MediaStatus.PROCESSED, "image/jpeg")))
								.toList());
		when(storage.exists(anyString())).thenReturn(true);
		when(thumbnails.findByMediaFileIdInAndVariant(anyList(), eq(MediaThumbnailVariant.SMALL)))
				.thenReturn(List.of());

		var response = service.publicMedia("wedding", session);

		assertThat(response).hasSize(500).noneMatch(item -> item.status() == MediaStatus.PROCESSING_FAILED);
	}

	@Test
	void managedMediaUsesSmallThumbnailOnlyWhenItExistsForTheVisibleStoredMedia() {
		stubAccessibleGallery();
		MediaFile visible = image("12345678-photo", "photo.jpg", MediaStatus.STORED, "image/jpeg");
		MediaThumbnail thumbnail = MediaThumbnail.builder().id("thumb-1").mediaFile(visible)
				.variant(MediaThumbnailVariant.SMALL).storageKey("thumbs/photo-small.jpg").sizeBytes(12).build();
		when(mediaFiles.findByGalleryIdAndStatusInOrderByStoredAtDescCreatedAtDescIdAsc(eq(gallery.getId()), any()))
				.thenReturn(List.of(visible));
		when(storage.exists(visible.getStorageKey())).thenReturn(true);
		when(thumbnails.findByMediaFileIdInAndVariant(List.of(visible.getId()), MediaThumbnailVariant.SMALL))
				.thenReturn(List.of(thumbnail));

		var response = service.managedMedia(event.getId(), gallery.getId(), owner.getEmail());

		assertThat(response).singleElement().satisfies(item -> {
			assertThat(item.thumbnailUrl())
					.isEqualTo("/api/events/event-1/galleries/gallery-1/media/" + visible.getId() + "/thumbnail");
			assertThat(item.contentUrl())
					.isEqualTo("/api/events/event-1/galleries/gallery-1/media/" + visible.getId() + "/content");
		});
	}

	@Test
	void managedMediaReturnsEmptyListWithoutThumbnailLookupWhenGalleryHasNoVisibleStoredFiles() {
		stubAccessibleGallery();
		when(mediaFiles.findByGalleryIdAndStatusInOrderByStoredAtDescCreatedAtDescIdAsc(eq(gallery.getId()), any()))
				.thenReturn(List.of());

		assertThat(service.managedMedia(event.getId(), gallery.getId(), owner.getEmail())).isEmpty();
		verifyNoInteractions(thumbnails);
	}

	@Test
	void managedMediaRejectsMissingGalleryBeforeReadingMedia() {
		when(eventService.requireUser(owner.getEmail())).thenReturn(owner);
		when(eventService.requireAccessibleEvent(event.getId(), owner)).thenReturn(event);
		when(galleries.findByIdAndEventIdAndDeletedAtIsNull(gallery.getId(), event.getId())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.managedMedia(event.getId(), gallery.getId(), owner.getEmail()))
				.isInstanceOfSatisfying(AppException.class,
						ex -> assertThat(ex.getErrorCode()).isEqualTo(GalleryErrorCode.GALLERY_NOT_FOUND));
		verifyNoInteractions(mediaFiles);
	}

	@Test
	void resourceStreamsThumbnailWhenPresentAndFallsBackToOriginalForMissingThumbnailOrVideos() {
		stubAccessibleGallery();
		MediaFile image = image("12345678-image", "photo.jpg", MediaStatus.PROCESSED, "image/jpeg");
		MediaFile video = video("12345678-video", "clip.mp4", "video/mp4");
		MediaThumbnail thumbnail = MediaThumbnail.builder().id("thumb-1").mediaFile(image)
				.variant(MediaThumbnailVariant.SMALL).storageKey("thumbs/photo-small.jpg").sizeBytes(123).build();
		when(mediaFiles.findByIdAndGalleryId(image.getId(), gallery.getId())).thenReturn(Optional.of(image));
		when(mediaFiles.findByIdAndGalleryId(video.getId(), gallery.getId())).thenReturn(Optional.of(video));
		when(storage.exists(image.getStorageKey())).thenReturn(true);
		when(storage.exists(video.getStorageKey())).thenReturn(true);
		when(thumbnails.findByMediaFileIdAndVariant(image.getId(), MediaThumbnailVariant.SMALL))
				.thenReturn(Optional.of(thumbnail), Optional.of(thumbnail));
		when(storage.exists(thumbnail.getStorageKey())).thenReturn(true, false);

		var thumbnailResource = service.managedResource(event.getId(), gallery.getId(), image.getId(), true,
				owner.getEmail());
		var imageFallback = service.managedResource(event.getId(), gallery.getId(), image.getId(), true,
				owner.getEmail());
		var videoOriginal = service.managedResource(event.getId(), gallery.getId(), video.getId(), true,
				owner.getEmail());

		assertThat(thumbnailResource.objectKey()).isEqualTo("thumbs/photo-small.jpg");
		assertThat(thumbnailResource.contentType()).isEqualTo("image/jpeg");
		assertThat(thumbnailResource.size()).isEqualTo(123);
		assertThat(imageFallback.objectKey()).isEqualTo(image.getStorageKey());
		assertThat(imageFallback.contentType()).isEqualTo("image/jpeg");
		assertThat(videoOriginal.contentType()).isEqualTo("video/mp4");
	}

	@Test
	void resourceRejectsMissingHiddenOrNotStoredMediaWithGenericGalleryError() {
		stubAccessibleGallery();
		MediaFile pending = image("12345678-pending", "pending.jpg", MediaStatus.PENDING, "image/jpeg");
		MediaFile missingObject = image("12345678-missing", "missing.jpg", MediaStatus.PROCESSED, "image/jpeg");
		when(mediaFiles.findByIdAndGalleryId("missing", gallery.getId())).thenReturn(Optional.empty());
		when(mediaFiles.findByIdAndGalleryId(pending.getId(), gallery.getId())).thenReturn(Optional.of(pending));
		when(mediaFiles.findByIdAndGalleryId(missingObject.getId(), gallery.getId()))
				.thenReturn(Optional.of(missingObject));
		when(storage.exists(missingObject.getStorageKey())).thenReturn(false);

		assertGalleryNotFound(
				() -> service.managedResource(event.getId(), gallery.getId(), "missing", false, owner.getEmail()));
		assertGalleryNotFound(() -> service.managedResource(event.getId(), gallery.getId(), pending.getId(), false,
				owner.getEmail()));
		assertGalleryNotFound(() -> service.managedResource(event.getId(), gallery.getId(), missingObject.getId(),
				false, owner.getEmail()));
	}

	@Test
	void ownerDownloadRequiresOwnerRoleAndSanitizesArchiveAndEntryNames() {
		stubAccessibleGallery();
		gallery.setDownloadEnabled(true);
		MediaFile dotted = image("12345678-dotted", "first photo.JPG", MediaStatus.PROCESSED, " ");
		MediaFile extensionless = image("87654321plain", "folder/second", MediaStatus.STORED, null);
		extensionless.setDeclaredContentType(null);
		gallery.setName(" ./Bad\u0000 Gallery// ");
		when(mediaFiles.findByGalleryIdAndStatusInOrderByStoredAtDescCreatedAtDescIdAsc(eq(gallery.getId()), any()))
				.thenReturn(List.of(dotted, extensionless));
		when(storage.exists(dotted.getStorageKey())).thenReturn(true);
		when(storage.exists(extensionless.getStorageKey())).thenReturn(true);
		when(eventService.roleFor(event, owner)).thenReturn(EventRole.MANAGER, EventRole.OWNER);

		assertThatThrownBy(() -> service.ownerDownload(event.getId(), gallery.getId(), owner.getEmail()))
				.isInstanceOfSatisfying(AppException.class,
						ex -> assertThat(ex.getErrorCode()).isEqualTo(EventErrorCode.EVENT_OWNER_REQUIRED));

		var download = service.ownerDownload(event.getId(), gallery.getId(), owner.getEmail());

		assertThat(download.fileName()).isEqualTo(".-Bad Gallery-.zip");
		assertThat(download.files()).extracting(file -> file.fileName()).containsExactly("first photo-12345678.JPG",
				"folder-second-87654321");
		assertThat(download.files()).extracting(file -> file.contentType()).containsExactly("image/jpeg",
				"application/octet-stream");
	}

	@Test
	void ownerDownloadRejectsDisabledGalleryAfterCheckingOwnership() {
		stubAccessibleGallery();
		when(eventService.roleFor(event, owner)).thenReturn(EventRole.OWNER);

		assertThatThrownBy(() -> service.ownerDownload(event.getId(), gallery.getId(), owner.getEmail()))
				.isInstanceOfSatisfying(AppException.class,
						ex -> assertThat(ex.getErrorCode()).isEqualTo(GalleryErrorCode.GALLERY_NOT_FOUND));
		verifyNoInteractions(mediaFiles);
	}

	@Test
	void publicResourceRejectsProcessingFailureWithGenericNotFound() {
		HttpSession session = new MockHttpSession();
		when(galleryAccess.requireGrant("wedding", session, false))
				.thenReturn(new GalleryAccessService.GrantedGallery(gallery, "access-1", session.getId()));
		MediaFile failed = image("12345678-failed", "failed.jpg", MediaStatus.PROCESSING_FAILED, "image/jpeg");
		when(mediaFiles.findByIdAndGalleryId(failed.getId(), gallery.getId())).thenReturn(Optional.of(failed));

		assertGalleryNotFound(() -> service.publicResource("wedding", failed.getId(), false, session));
	}

	@Test
	void ownerDownloadUsesSafeFallbackArchiveNameForNullOrDotGalleryNames() {
		stubAccessibleGallery();
		gallery.setDownloadEnabled(true);
		when(eventService.roleFor(event, owner)).thenReturn(EventRole.OWNER);
		when(mediaFiles.findByGalleryIdAndStatusInOrderByStoredAtDescCreatedAtDescIdAsc(eq(gallery.getId()), any()))
				.thenReturn(List.of());

		gallery.setName(null);
		assertThat(service.ownerDownload(event.getId(), gallery.getId(), owner.getEmail()).fileName())
				.isEqualTo("gallery.zip");
		gallery.setName(".");
		assertThat(service.ownerDownload(event.getId(), gallery.getId(), owner.getEmail()).fileName())
				.isEqualTo("gallery.zip");
		gallery.setName("..");
		assertThat(service.ownerDownload(event.getId(), gallery.getId(), owner.getEmail()).fileName())
				.isEqualTo("gallery.zip");
	}

	@Test
	void openDelegatesToStorageForControllerStreaming() {
		when(storage.open("objects/file")).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));

		assertThat(service.open("objects/file")).hasBinaryContent(new byte[]{1, 2, 3});
	}

	private void stubAccessibleGallery() {
		when(eventService.requireUser(owner.getEmail())).thenReturn(owner);
		when(eventService.requireAccessibleEvent(event.getId(), owner)).thenReturn(event);
		when(galleries.findByIdAndEventIdAndDeletedAtIsNull(gallery.getId(), event.getId())).thenReturn(Optional.of(gallery));
	}

	private MediaFile image(String id, String fileName, MediaStatus status, String detectedContentType) {
		return MediaFile.builder().id(id).gallery(gallery).originalFilename(fileName).storageKey("objects/" + id)
				.expectedSizeBytes(100).sizeBytes(80L).declaredContentType("image/jpeg")
				.detectedContentType(detectedContentType).mediaType(MediaType.IMAGE).status(status)
				.publicationStatus(pl.backend.weddinggallery.media.model.PublicationStatus.APPROVED)
				.storedAt(LocalDateTime.parse("2026-07-23T18:00:00")).build();
	}

	private MediaFile video(String id, String fileName, String declaredContentType) {
		return MediaFile.builder().id(id).gallery(gallery).originalFilename(fileName).storageKey("objects/" + id)
				.expectedSizeBytes(200).sizeBytes(null).declaredContentType(declaredContentType)
				.mediaType(MediaType.VIDEO).status(MediaStatus.STORED)
				.storedAt(LocalDateTime.parse("2026-07-23T18:00:00")).build();
	}

	private void assertGalleryNotFound(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
		assertThatThrownBy(call).isInstanceOfSatisfying(AppException.class,
				ex -> assertThat(ex.getErrorCode()).isEqualTo(GalleryErrorCode.PUBLIC_GALLERY_NOT_FOUND));
	}
}
