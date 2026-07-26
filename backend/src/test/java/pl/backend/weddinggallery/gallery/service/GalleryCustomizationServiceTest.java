package pl.backend.weddinggallery.gallery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.event.service.EventService;
import pl.backend.weddinggallery.gallery.dto.GalleryCustomizationRequest;
import pl.backend.weddinggallery.gallery.exception.GalleryErrorCode;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.membership.model.EventRole;
import pl.backend.weddinggallery.media.model.MediaFile;
import pl.backend.weddinggallery.media.model.MediaStatus;
import pl.backend.weddinggallery.media.repository.MediaFileRepository;
import pl.backend.weddinggallery.user.model.User;

@ExtendWith(MockitoExtension.class)
class GalleryCustomizationServiceTest {
	@Mock
	GalleryRepository galleries;
	@Mock
	EventService events;
	@Mock
	AuditService audit;
	@Mock
	MediaFileRepository mediaFiles;
	private GalleryCustomizationService service;
	private Gallery gallery;
	private Event event;
	private User owner;

	@BeforeEach
	void setUp() {
		service = new GalleryCustomizationService(galleries, events, audit, mediaFiles);
		owner = User.builder().id("owner").email("owner@example.com").build();
		event = Event.builder().id("event").owner(owner).build();
		gallery = Gallery.builder().id("gallery").event(event).version(3).theme("EDITORIAL").layout("GRID")
				.primaryColor("#111111").accentColor("#222222").backgroundColor("#333333").welcomeText("old")
				.showTitle(true).showUpload(true).showDownload(false).build();
		when(galleries.findById("gallery")).thenReturn(Optional.of(gallery));
		lenient().when(events.requireUser(owner.getEmail())).thenReturn(owner);
		lenient().when(events.requireAccessibleEvent("event", owner)).thenReturn(event);
		lenient().when(events.roleFor(event, owner)).thenReturn(EventRole.OWNER);
	}

	@Test
	void getReturnsExistingCustomizationForAccessibleUser() {
		var response = service.get("gallery", owner.getEmail());

		assertThat(response.theme()).isEqualTo("EDITORIAL");
		assertThat(response.welcomeText()).isEqualTo("old");
		assertThat(response.version()).isEqualTo(3);
		verifyNoInteractions(audit);
	}

	@Test
	void updateNormalizesColorsAndNullWelcomeTextAndAllowsProcessedCover() {
		MediaFile cover = MediaFile.builder().id("cover").gallery(gallery).status(MediaStatus.PROCESSED).build();
		when(mediaFiles.findByIdAndGalleryId("cover", "gallery")).thenReturn(Optional.of(cover));
		var request = new GalleryCustomizationRequest("MINIMAL", "MASONRY", "#aabbcc", "#ddeeff", "#010203", null,
				false, true, true, "cover", 3);

		var response = service.update("gallery", request, owner.getEmail());

		assertThat(response.primaryColor()).isEqualTo("#AABBCC");
		assertThat(response.accentColor()).isEqualTo("#DDEEFF");
		assertThat(response.backgroundColor()).isEqualTo("#010203");
		assertThat(response.welcomeText()).isEmpty();
		assertThat(gallery.getCoverMediaId()).isEqualTo("cover");
		verify(galleries).save(gallery);
	}

	@Test
	void updateRejectsStaleVersionForeignRoleAndInvalidCover() {
		var request = new GalleryCustomizationRequest("MINIMAL", "GRID", "#aabbcc", "#ddeeff", "#010203", "welcome",
				true, true, true, null, 2);
		assertThatThrownBy(() -> service.update("gallery", request, owner.getEmail())).isInstanceOfSatisfying(
				AppException.class,
				e -> assertThat(e.getErrorCode()).isEqualTo(GalleryErrorCode.GALLERY_CUSTOMIZATION_CONFLICT));

		User member = User.builder().id("member").email("member@example.com").build();
		when(events.requireUser(member.getEmail())).thenReturn(member);
		when(events.requireAccessibleEvent("event", member)).thenReturn(event);
		when(events.roleFor(event, member)).thenReturn(EventRole.MANAGER);
		var current = new GalleryCustomizationRequest("MINIMAL", "GRID", "#aabbcc", "#ddeeff", "#010203", "welcome",
				true, true, true, null, 3);
		assertThatThrownBy(() -> service.update("gallery", current, member.getEmail())).isInstanceOfSatisfying(
				AppException.class,
				e -> assertThat(e.getErrorCode()).isEqualTo(GalleryErrorCode.GALLERY_SETTINGS_OWNER_REQUIRED));

		when(mediaFiles.findByIdAndGalleryId("cover", "gallery")).thenReturn(
				Optional.of(MediaFile.builder().id("cover").gallery(gallery).status(MediaStatus.PROCESSING).build()));
		var invalidCover = new GalleryCustomizationRequest("MINIMAL", "GRID", "#aabbcc", "#ddeeff", "#010203",
				"welcome", true, true, true, "cover", 3);
		assertThatThrownBy(() -> service.update("gallery", invalidCover, owner.getEmail())).isInstanceOfSatisfying(
				AppException.class,
				e -> assertThat(e.getErrorCode()).isEqualTo(GalleryErrorCode.GALLERY_CUSTOMIZATION_COVER_INVALID));
	}

	@Test
    void missingOrDeletedGalleryIsNotFound() {
        when(galleries.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get("missing", owner.getEmail()))
                .isInstanceOfSatisfying(AppException.class, e -> assertThat(e.getErrorCode())
                        .isEqualTo(GalleryErrorCode.GALLERY_NOT_FOUND));
        gallery.setDeletedAt(java.time.LocalDateTime.now());
        assertThatThrownBy(() -> service.get("gallery", owner.getEmail()))
                .isInstanceOfSatisfying(AppException.class, e -> assertThat(e.getErrorCode())
                        .isEqualTo(GalleryErrorCode.GALLERY_NOT_FOUND));
    }
}
