package pl.backend.weddinggallery.publicaccess.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
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
import pl.backend.weddinggallery.publicaccess.model.GalleryAccess;
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
		event.setStatus(EventStatus.DELETED);
		assertUnavailable(request);
	}

	@Test
	void settingsRejectInvalidFlagAndPublicationBoundaryCombinations() {
		stubOwner();
		when(accesses.findByGalleryIdAndRevokedAtIsNull(gallery.getId())).thenReturn(List.of(activeAccess()));

		var uploadWithoutView = new GallerySettingsRequest(false, true, false, ModerationMode.REQUIRED, null, null, 7);
		assertCode(() -> service.updateSettings(event.getId(), gallery.getId(), uploadWithoutView, actor.getEmail()),
				GalleryErrorCode.GALLERY_INVALID_PUBLICATION_WINDOW);

		Instant start = Instant.parse("2030-01-01T12:00:00Z");
		var equalWindow = new GallerySettingsRequest(true, false, false, ModerationMode.REQUIRED, start, start, 7);
		assertCode(() -> service.updateSettings(event.getId(), gallery.getId(), equalWindow, actor.getEmail()),
				GalleryErrorCode.GALLERY_INVALID_PUBLICATION_WINDOW);
		var reversedWindow = new GallerySettingsRequest(true, false, false, ModerationMode.REQUIRED,
				start.plusSeconds(1), start, 7);
		assertCode(() -> service.updateSettings(event.getId(), gallery.getId(), reversedWindow, actor.getEmail()),
				GalleryErrorCode.GALLERY_INVALID_PUBLICATION_WINDOW);
	}

	@Test
	void ownerCanUpdateEverySettingWithOpenAndBoundedPublicationWindows() {
		stubOwner();
		when(accesses.findByGalleryIdAndRevokedAtIsNull(gallery.getId())).thenReturn(List.of(activeAccess()));
		Instant start = Instant.parse("2030-01-01T12:00:00Z");
		Instant end = start.plusSeconds(1);

		var response = service.updateSettings(event.getId(), gallery.getId(),
				new GallerySettingsRequest(true, false, true, ModerationMode.NONE, start, end, 7), actor.getEmail());

		assertThat(response.publishedAt()).isEqualTo(start);
		assertThat(response.expiresAt()).isEqualTo(end);
		assertThat(response.downloadEnabled()).isTrue();
		verify(galleries).flush();

		gallery.setVersion(8);
		service.updateSettings(event.getId(), gallery.getId(),
				new GallerySettingsRequest(false, false, false, ModerationMode.REQUIRED, null, null, 8),
				actor.getEmail());
		assertThat(gallery.getPublishedAt()).isNull();
		assertThat(gallery.getExpiresAt()).isNull();
	}

	@Test
	void tokenAndAccessCodeRotationRevokeOldCredentialsAndAuditEveryMutation() {
		stubOwner();
		GalleryAccess first = activeAccess();
		GalleryAccess second = GalleryAccess.builder().id("access-2").gallery(gallery).build();
		when(accesses.findByGalleryIdAndRevokedAtIsNull(gallery.getId())).thenReturn(List.of(first, second));
		when(tokens.generate()).thenReturn("raw-token");
		when(tokens.hash("raw-token")).thenReturn("hash");
		when(passwords.encode("secret-code")).thenReturn("encoded");

		var token = service.rotateToken(event.getId(), gallery.getId(), actor.getEmail());
		service.setAccessCode(event.getId(), gallery.getId(), "secret-code", actor.getEmail());
		assertThat(gallery.getAccessCodeHash()).isEqualTo("encoded");
		service.clearAccessCode(event.getId(), gallery.getId(), actor.getEmail());

		assertThat(token.accessToken()).isEqualTo("raw-token");
		assertThat(token.sharePath()).contains("reception", "raw-token");
		assertThat(first.getRevokedAt()).isNotNull();
		assertThat(second.getRevokedAt()).isNotNull();
		assertThat(gallery.getAccessCodeHash()).isNull();
		verify(accesses).save(argThat(saved -> saved.getGallery() == gallery && "hash".equals(saved.getTokenHash())));
	}

	@Test
	void exchangeRequiresAndVerifiesCodeThenGrantHonorsUploadAndRevocation() {
		GalleryAccess access = activeAccess();
		stubAvailableAccess(access);
		gallery.setAccessCodeHash("encoded");
		MockHttpSession session = new MockHttpSession();

		assertCode(() -> service.exchange(gallery.getSlug(), new PublicAccessRequest("token", null), session),
				GalleryErrorCode.GALLERY_ACCESS_CODE_REQUIRED);
		assertCode(() -> service.exchange(gallery.getSlug(), new PublicAccessRequest("token", "  "), session),
				GalleryErrorCode.GALLERY_ACCESS_CODE_REQUIRED);
		when(passwords.matches("wrong", "encoded")).thenReturn(false);
		assertCode(() -> service.exchange(gallery.getSlug(), new PublicAccessRequest("token", "wrong"), session),
				GalleryErrorCode.GALLERY_ACCESS_DENIED);

		when(passwords.matches("correct", "encoded")).thenReturn(true);
		when(accesses.findById(access.getId())).thenReturn(Optional.of(access));
		var exchanged = service.exchange(gallery.getSlug(), new PublicAccessRequest("token", "correct"), session);
		assertThat(exchanged.slug()).isEqualTo("reception");
		assertThat(service.getPublic(gallery.getSlug(), session).slug()).isEqualTo("reception");
		assertThat(service.requireGrant(gallery.getSlug(), session, true).httpSessionId()).isEqualTo(session.getId());

		gallery.setUploadEnabled(false);
		assertCode(() -> service.requireGrant(gallery.getSlug(), session, true),
				GalleryErrorCode.PUBLIC_GALLERY_NOT_FOUND);
		assertThat(service.requireGrant(gallery.getSlug(), session, false).gallery()).isSameAs(gallery);

		access.setRevokedAt(LocalDateTime.now());
		assertCode(() -> service.getPublic(gallery.getSlug(), session), GalleryErrorCode.PUBLIC_GALLERY_NOT_FOUND);
	}

	@Test
	void publicGrantRejectsMissingTokenAndMutableOperationsRejectArchivedParents() {
		when(galleries.findBySlugAndDeletedAtIsNull("missing")).thenReturn(Optional.empty());
		assertCode(() -> service.exchange("missing", new PublicAccessRequest("token", null), new MockHttpSession()),
				GalleryErrorCode.PUBLIC_GALLERY_NOT_FOUND);
		assertCode(() -> service.getPublic("missing", new MockHttpSession()),
				GalleryErrorCode.PUBLIC_GALLERY_NOT_FOUND);

		stubOwner();
		gallery.setStatus(GalleryStatus.ARCHIVED);
		assertCode(() -> service.rotateToken(event.getId(), gallery.getId(), actor.getEmail()),
				GalleryErrorCode.GALLERY_ARCHIVED);
		gallery.setStatus(GalleryStatus.ACTIVE);
		event.setStatus(EventStatus.DELETED);
		assertCode(() -> service.setAccessCode(event.getId(), gallery.getId(), "code", actor.getEmail()),
				GalleryErrorCode.GALLERY_ARCHIVED);
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

	private void stubOwner() {
		stubAccessibleContext();
		when(events.roleFor(event, actor)).thenReturn(EventRole.OWNER);
	}

	private GalleryAccess activeAccess() {
		return GalleryAccess.builder().id("access-1").gallery(gallery).tokenHash("hash").build();
	}

	private void stubAvailableAccess(GalleryAccess access) {
		when(galleries.findBySlugAndDeletedAtIsNull(gallery.getSlug())).thenReturn(Optional.of(gallery));
		when(tokens.hash("token")).thenReturn("hash");
		when(accesses.findByGalleryIdAndTokenHashAndRevokedAtIsNull(gallery.getId(), "hash"))
				.thenReturn(Optional.of(access));
	}

	private void assertCode(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, GalleryErrorCode code) {
		assertThatThrownBy(call).isInstanceOfSatisfying(AppException.class,
				error -> assertThat(error.getErrorCode()).isEqualTo(code));
	}

	private void assertUnavailable(PublicAccessRequest request) {
		assertThatThrownBy(() -> service.exchange(gallery.getSlug(), request, new MockHttpSession()))
				.isInstanceOf(AppException.class)
				.satisfies(error -> org.assertj.core.api.Assertions.assertThat(((AppException) error).getErrorCode())
						.isEqualTo(GalleryErrorCode.PUBLIC_GALLERY_NOT_FOUND));
	}
}
