package pl.backend.weddinggallery.media.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.exception.EventErrorCode;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.event.service.EventService;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.media.dto.*;
import pl.backend.weddinggallery.media.exception.MediaModerationErrorCode;
import pl.backend.weddinggallery.media.model.MediaFile;
import pl.backend.weddinggallery.media.model.PublicationStatus;
import pl.backend.weddinggallery.media.repository.MediaFileRepository;
import pl.backend.weddinggallery.user.model.User;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediaModerationServiceTest {
	@Mock
	private MediaFileRepository mediaFiles;
	@Mock
	private GalleryRepository galleries;
	@Mock
	private EventService eventService;
	@Mock
	private AuditService auditService;
	private MediaModerationService service;
	private User owner;
	private Event event;
	private Gallery gallery;
	private MediaFile media;

	@BeforeEach
	void setUp() {
		service = new MediaModerationService(mediaFiles, galleries, eventService, auditService);
		owner = User.builder().id("owner").email("owner@example.com").build();
		event = Event.builder().id("event").owner(owner).build();
		gallery = Gallery.builder().id("gallery").event(event).build();
		media = MediaFile.builder().id("media").gallery(gallery).publicationStatus(PublicationStatus.PENDING).build();
		when(eventService.requireUser(owner.getEmail())).thenReturn(owner);
		when(eventService.requireAccessibleEvent(event.getId(), owner)).thenReturn(event);
		when(galleries.findByIdAndEventIdAndDeletedAtIsNull(gallery.getId(), event.getId()))
				.thenReturn(Optional.of(gallery));
		lenient().when(mediaFiles.findByIdAndGalleryId(anyString(), eq(gallery.getId())))
				.thenReturn(Optional.of(media));
	}

	@Test
	void approveChangesPublicationStatusAndAuditsTheDecision() {
		MediaModerationResponse response = service.moderate(event.getId(), gallery.getId(), media.getId(),
				MediaModerationAction.APPROVE, owner.getEmail());

		assertThat(response).isEqualTo(new MediaModerationResponse("media", PublicationStatus.APPROVED, true));
		verify(auditService).logRequiredGalleryEvent(owner.getEmail(), EventType.MEDIA_PUBLICATION_APPROVED, "event",
				"gallery", "mediaId=media,from=PENDING,to=APPROVED");
	}

	@Test
	void replayIsSuccessfulButDoesNotWriteASecondAuditEvent() {
		media.setPublicationStatus(PublicationStatus.HIDDEN);

		MediaModerationResponse response = service.moderate(event.getId(), gallery.getId(), media.getId(),
				MediaModerationAction.HIDE, "duplicate content", owner.getEmail());

		assertThat(response.changed()).isFalse();
		verifyNoInteractions(auditService);
	}

	@Test
	void bulkSupportsOneHundredItemsAndRejectsTheOneHundredAndFirst() {
		List<String> ids = java.util.stream.IntStream.range(0, 100).mapToObj(i -> "media-" + i).toList();
		when(mediaFiles.findByIdAndGalleryId(anyString(), eq(gallery.getId())))
				.thenAnswer(invocation -> Optional.of(MediaFile.builder().id(invocation.getArgument(0)).gallery(gallery)
						.publicationStatus(PublicationStatus.PENDING).build()));

		assertThat(service.bulk(event.getId(), gallery.getId(),
				new BulkMediaModerationRequest(ids, MediaModerationAction.REJECT, "duplicate content"),
				owner.getEmail())).hasSize(100);

		List<String> tooMany = java.util.stream.IntStream.range(0, 101).mapToObj(i -> "media-" + i).toList();
		assertThatThrownBy(() -> service.bulk(event.getId(), gallery.getId(),
				new BulkMediaModerationRequest(tooMany, MediaModerationAction.REJECT, "duplicate content"),
				owner.getEmail()))
				.isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getErrorCode())
						.isEqualTo(MediaModerationErrorCode.MEDIA_MODERATION_BULK_LIMIT_EXCEEDED));
	}

	@Test
	void outsiderCannotModerateAndCrossGalleryMediaIsNotFound() {
		when(eventService.requireUser("outsider@example.com")).thenReturn(User.builder().id("outsider").build());
		when(eventService.requireAccessibleEvent(eq(event.getId()), argThat(user -> "outsider".equals(user.getId()))))
				.thenThrow(new AppException(EventErrorCode.EVENT_NOT_FOUND));

		assertThatThrownBy(() -> service.moderate(event.getId(), gallery.getId(), media.getId(),
				MediaModerationAction.APPROVE, "outsider@example.com")).isInstanceOfSatisfying(AppException.class,
				ex -> assertThat(ex.getErrorCode()).isEqualTo(EventErrorCode.EVENT_NOT_FOUND));

		when(mediaFiles.findByIdAndGalleryId("foreign", gallery.getId())).thenReturn(Optional.empty());
		assertThatThrownBy(() -> service.moderate(event.getId(), gallery.getId(), "foreign", MediaModerationAction.HIDE,
				"foreign media", owner.getEmail())).isInstanceOfSatisfying(AppException.class,
				ex -> assertThat(ex.getErrorCode()).isEqualTo(MediaModerationErrorCode.MEDIA_MODERATION_NOT_FOUND));
	}

	@Test
	void allModerationActionsEnforceReasonsTransitionsAndAuditTypes() {
		media.setPublicationStatus(PublicationStatus.PENDING);
		service.moderate(event.getId(), gallery.getId(), media.getId(), MediaModerationAction.REJECT, "copyright",
				owner.getEmail());
		assertThat(media.getPublicationStatus()).isEqualTo(PublicationStatus.REJECTED);

		service.moderate(event.getId(), gallery.getId(), media.getId(), MediaModerationAction.RESTORE,
				owner.getEmail());
		assertThat(media.getPublicationStatus()).isEqualTo(PublicationStatus.APPROVED);

		service.moderate(event.getId(), gallery.getId(), media.getId(), MediaModerationAction.HIDE, "privacy",
				owner.getEmail());
		assertThat(media.getPublicationStatus()).isEqualTo(PublicationStatus.HIDDEN);

		service.moderate(event.getId(), gallery.getId(), media.getId(), MediaModerationAction.RESTORE,
				owner.getEmail());
		assertThat(media.getPublicationStatus()).isEqualTo(PublicationStatus.APPROVED);
		verify(auditService, times(4)).logRequiredGalleryEvent(anyString(), any(), eq("event"), eq("gallery"),
				anyString());
	}

	@Test
	void invalidReasonsBulkShapeAndTransitionsAreRejected() {
		assertThatThrownBy(() -> service.moderate(event.getId(), gallery.getId(), media.getId(),
				MediaModerationAction.REJECT, " ", owner.getEmail()))
				.isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getErrorCode())
						.isEqualTo(MediaModerationErrorCode.MEDIA_MODERATION_REASON_REQUIRED));
		assertThatThrownBy(() -> service.bulk(event.getId(), gallery.getId(), null, owner.getEmail()))
				.isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getErrorCode())
						.isEqualTo(MediaModerationErrorCode.MEDIA_MODERATION_EMPTY_BULK));
		assertThatThrownBy(() -> service.bulk(event.getId(), gallery.getId(),
				new BulkMediaModerationRequest(List.of("media", "media"), MediaModerationAction.APPROVE),
				owner.getEmail()))
				.isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getErrorCode())
						.isEqualTo(MediaModerationErrorCode.MEDIA_MODERATION_DUPLICATE_MEDIA));
		media.setPublicationStatus(PublicationStatus.PENDING);
		assertThatThrownBy(() -> service.moderate(event.getId(), gallery.getId(), media.getId(),
				MediaModerationAction.HIDE, "privacy", owner.getEmail()))
				.isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getErrorCode())
						.isEqualTo(MediaModerationErrorCode.MEDIA_MODERATION_INVALID_TRANSITION));
	}
}
