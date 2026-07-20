package pl.backend.weddinggallery.publicaccess.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.event.model.EventStatus;
import pl.backend.weddinggallery.event.service.EventService;
import pl.backend.weddinggallery.gallery.exception.GalleryErrorCode;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.model.GalleryStatus;
import pl.backend.weddinggallery.gallery.model.ModerationMode;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.membership.model.EventRole;
import pl.backend.weddinggallery.publicaccess.dto.GallerySettingsRequest;
import pl.backend.weddinggallery.publicaccess.dto.PublicAccessRequest;
import pl.backend.weddinggallery.publicaccess.repository.GalleryAccessRepository;
import pl.backend.weddinggallery.user.model.User;

@ExtendWith(MockitoExtension.class)
class GalleryAccessServiceTest {
	@Mock
	private GalleryRepository galleries;
	@Mock
	private GalleryAccessRepository accesses;
	@Mock
	private EventService events;
	@Mock
	private PasswordEncoder passwords;
	@Mock
	private TokenService tokens;
	@Mock
	private AuditService audit;
	private GalleryAccessService service;
	private User actor;
	private Event event;
	private Gallery gallery;

	@BeforeEach
	void setUp() {
		service = new GalleryAccessService(galleries, accesses, events, passwords, tokens, audit);
		actor = User.builder().id("user-1").email("manager@example.com").build();
		event = Event.builder().id("event-1").status(EventStatus.DRAFT).owner(actor).build();
		gallery = Gallery.builder().id("gallery-1").event(event).slug("reception").name("Reception")
				.status(GalleryStatus.ACTIVE).moderationMode(ModerationMode.REQUIRED).publicViewEnabled(true)
				.uploadEnabled(true).version(7).build();
	}

	@Test
	void managerCanReadSettingsButCannotMutateThem() {
		stubAccessibleContext();
		when(events.roleFor(event, actor)).thenReturn(EventRole.MANAGER);
		when(accesses.findByGalleryIdAndRevokedAtIsNull(gallery.getId())).thenReturn(List.of());

		service.getSettings(event.getId(), gallery.getId(), actor.getEmail());

		assertThatThrownBy(() -> service.updateSettings(event.getId(), gallery.getId(), settings(7), actor.getEmail()))
				.isInstanceOf(AppException.class)
				.satisfies(error -> org.assertj.core.api.Assertions.assertThat(((AppException) error).getErrorCode())
						.isEqualTo(GalleryErrorCode.GALLERY_SETTINGS_OWNER_REQUIRED));
	}

	@Test
	void ownerCannotEnablePublicViewWithoutAnActiveToken() {
		stubAccessibleContext();
		when(events.roleFor(event, actor)).thenReturn(EventRole.OWNER);
		when(accesses.findByGalleryIdAndRevokedAtIsNull(gallery.getId())).thenReturn(List.of());

		assertThatThrownBy(() -> service.updateSettings(event.getId(), gallery.getId(), settings(7), actor.getEmail()))
				.isInstanceOf(AppException.class)
				.satisfies(error -> org.assertj.core.api.Assertions.assertThat(((AppException) error).getErrorCode())
						.isEqualTo(GalleryErrorCode.GALLERY_ACCESS_TOKEN_REQUIRED));
	}

	@Test
	void staleSettingsVersionIsRejectedBeforeAnyMutation() {
		stubAccessibleContext();
		when(events.roleFor(event, actor)).thenReturn(EventRole.OWNER);

		assertThatThrownBy(() -> service.updateSettings(event.getId(), gallery.getId(), settings(6), actor.getEmail()))
				.isInstanceOf(org.springframework.orm.ObjectOptimisticLockingFailureException.class);
	}

	@Test
	void allUnavailablePublicStatesUseTheSameGenericError() {
		when(galleries.findBySlugAndDeletedAtIsNull(gallery.getSlug())).thenReturn(Optional.of(gallery));
		PublicAccessRequest request = new PublicAccessRequest("token", null);

		gallery.setPublicViewEnabled(false);
		assertUnavailable(request);
		gallery.setPublicViewEnabled(true);
		gallery.setPublishedAt(LocalDateTime.now().plusMinutes(5));
		assertUnavailable(request);
		gallery.setPublishedAt(null);
		gallery.setExpiresAt(LocalDateTime.now().minusSeconds(1));
		assertUnavailable(request);
		gallery.setExpiresAt(null);
		gallery.setStatus(GalleryStatus.ARCHIVED);
		assertUnavailable(request);
		gallery.setStatus(GalleryStatus.ACTIVE);
		event.setStatus(EventStatus.ARCHIVED);
		assertUnavailable(request);
	}

	private GallerySettingsRequest settings(long version) {
		return new GallerySettingsRequest(true, true, false, ModerationMode.REQUIRED, null, null, version);
	}

	private void stubAccessibleContext() {
		when(events.requireUser(actor.getEmail())).thenReturn(actor);
		when(events.requireAccessibleEvent(event.getId(), actor)).thenReturn(event);
		when(galleries.findByIdAndEventIdAndDeletedAtIsNull(gallery.getId(), event.getId()))
				.thenReturn(Optional.of(gallery));
	}

	private void assertUnavailable(PublicAccessRequest request) {
		assertThatThrownBy(() -> service.exchange(gallery.getSlug(), request, new MockHttpSession()))
				.isInstanceOf(AppException.class)
				.satisfies(error -> org.assertj.core.api.Assertions.assertThat(((AppException) error).getErrorCode())
						.isEqualTo(GalleryErrorCode.PUBLIC_GALLERY_NOT_FOUND));
	}
}
