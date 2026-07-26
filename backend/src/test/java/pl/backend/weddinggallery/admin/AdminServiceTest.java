package pl.backend.weddinggallery.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import pl.backend.weddinggallery.admin.service.AdminService;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.repository.AuditEventRepository;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.event.model.EventStatus;
import pl.backend.weddinggallery.event.repository.EventRepository;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.media.model.MediaFile;
import pl.backend.weddinggallery.media.model.PublicationStatus;
import pl.backend.weddinggallery.media.repository.MediaFileRepository;
import pl.backend.weddinggallery.user.model.SystemRole;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
	@Mock
	private UserRepository userRepository;
	@Mock
	private EventRepository eventRepository;
	@Mock
	private pl.backend.weddinggallery.gallery.repository.GalleryRepository galleryRepository;
	@Mock
	private MediaFileRepository mediaFileRepository;
	@Mock
	private AuditEventRepository auditEventRepository;
	@Mock
	private AuditService auditService;
	@InjectMocks
	private AdminService service;

	private User admin;
	private User target;

	@BeforeEach
	void setUp() {
		admin = User.builder().id("admin").email("admin@example.com").systemRole(SystemRole.ADMIN).build();
		target = User.builder().id("target").email("target@example.com").systemRole(SystemRole.USER).build();
	}

	@Test
	void roleCheckIsFailClosedAndDoesNotTrustAuthenticationName() {
		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn(new Object());
		when(auth.getName()).thenReturn("ADMIN@example.com");
		when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
		assertThat(service.isAdmin(auth)).isTrue();

		when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(target));
		when(auth.getName()).thenReturn("user@example.com");
		assertThat(service.isAdmin(auth)).isFalse();
	}

	@Test
	void lockAndUnlockAreIdempotentAndAuditOnlyRealMutations() {
		when(userRepository.findById("target")).thenReturn(Optional.of(target));
		service.lockUser("admin@example.com", "target");
		assertThat(target.getLockedUntil()).isAfter(LocalDateTime.now());
		verify(auditService).logRequiredEvent("admin@example.com", EventType.USER_LOGIN_BLOCKED, null, "target",
				"ADMIN_ACTION=LOCK_USER");

		clearInvocations(auditService, userRepository);
		service.lockUser("admin@example.com", "target");
		verifyNoInteractions(auditService);

		target.setFailedLoginAttempts(2);
		service.unlockUser("admin@example.com", "target");
		assertThat(target.getLockedUntil()).isNull();
		assertThat(target.getFailedLoginAttempts()).isZero();
		verify(auditService).logRequiredEvent("admin@example.com", EventType.USER_LOGIN_BLOCKED, null, "target",
				"ADMIN_ACTION=UNLOCK_USER");
	}

	@Test
	void archiveAndHideAreScopedToExistingResourcesAndAuditTheMutation() {
		Event event = Event.builder().id("event").name("Wedding").owner(admin).status(EventStatus.DRAFT).build();
		when(eventRepository.findById("event")).thenReturn(Optional.of(event));
		service.archiveEvent("admin@example.com", "event");
		assertThat(event.getStatus()).isEqualTo(EventStatus.ARCHIVED);
		verify(auditService).logRequiredEvent("admin@example.com", EventType.EVENT_ARCHIVED, "event", null,
				"ADMIN_ACTION=ARCHIVE_EVENT");

		Gallery gallery = Gallery.builder().id("gallery").event(event).name("Main").slug("main").build();
		MediaFile media = MediaFile.builder().id("media").gallery(gallery).originalFilename("photo.jpg")
				.publicationStatus(PublicationStatus.APPROVED).build();
		when(mediaFileRepository.findById("media")).thenReturn(Optional.of(media));
		service.hideMedia("admin@example.com", "media");
		assertThat(media.getPublicationStatus()).isEqualTo(PublicationStatus.HIDDEN);
		verify(auditService).logRequiredGalleryEvent("admin@example.com", EventType.MEDIA_PUBLICATION_HIDDEN, "event",
				"gallery", "ADMIN_ACTION=HIDE_MEDIA mediaId=media");

		when(eventRepository.findById("missing")).thenReturn(Optional.empty());
		assertThatThrownBy(() -> service.archiveEvent("admin@example.com", "missing"))
				.isInstanceOf(AdminService.AdminNotFoundException.class);
	}

	@Test
	void adminChecksAndAdministrativeMutationsAreFailClosedAndIdempotent() {
		assertThat(service.isAdmin(null)).isFalse();
		Authentication unauthenticated = mock(Authentication.class);
		when(unauthenticated.isAuthenticated()).thenReturn(false);
		assertThat(service.isAdmin(unauthenticated)).isFalse();
		Authentication anonymous = mock(Authentication.class);
		when(anonymous.isAuthenticated()).thenReturn(true);
		when(anonymous.getPrincipal()).thenReturn("anonymousUser");
		assertThat(service.isAdmin(anonymous)).isFalse();

		Authentication unknown = mock(Authentication.class);
		when(unknown.isAuthenticated()).thenReturn(true);
		when(unknown.getPrincipal()).thenReturn(new Object());
		when(unknown.getName()).thenReturn("unknown@example.com");
		when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
		assertThat(service.isAdmin(unknown)).isFalse();

		when(userRepository.findById("target")).thenReturn(Optional.of(target));
		service.unlockUser("admin@example.com", "target");
		verify(userRepository, never()).save(target);

		Event archived = Event.builder().id("archived").name("Archived").owner(admin).status(EventStatus.ARCHIVED)
				.build();
		when(eventRepository.findById("archived")).thenReturn(Optional.of(archived));
		service.archiveEvent("admin@example.com", "archived");
		verify(eventRepository, never()).save(archived);
		Event deleted = Event.builder().id("deleted").name("Deleted").owner(admin).status(EventStatus.DRAFT)
				.deletedAt(LocalDateTime.now()).build();
		when(eventRepository.findById("deleted")).thenReturn(Optional.of(deleted));
		service.archiveEvent("admin@example.com", "deleted");
		verify(eventRepository, never()).save(deleted);

		Gallery gallery = Gallery.builder().id("gallery").event(archived).build();
		MediaFile hidden = MediaFile.builder().id("hidden").gallery(gallery).publicationStatus(PublicationStatus.HIDDEN)
				.build();
		when(mediaFileRepository.findById("hidden")).thenReturn(Optional.of(hidden));
		service.hideMedia("admin@example.com", "hidden");
		verify(mediaFileRepository, never()).save(hidden);
	}
}
