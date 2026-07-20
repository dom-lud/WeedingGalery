package pl.backend.weddinggallery.gallery.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.event.model.EventStatus;
import pl.backend.weddinggallery.event.service.EventService;
import pl.backend.weddinggallery.gallery.dto.GalleryWriteRequest;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.membership.model.EventRole;
import pl.backend.weddinggallery.user.model.User;

@ExtendWith(MockitoExtension.class)
class GalleryServiceTest {
	@Mock
	private GalleryRepository galleryRepository;
	@Mock
	private EventService eventService;
	@Mock
	private AuditService auditService;

	private GalleryService service;

	@BeforeEach
	void setUp() {
		service = new GalleryService(galleryRepository, eventService, auditService);
	}

	@Test
	void shouldPropagateRequiredGalleryAuditFailure() {
		User owner = User.builder().id("owner-id").email("owner@example.com").build();
		Event event = Event.builder().id("event-id").owner(owner).status(EventStatus.DRAFT).build();
		when(eventService.requireUser("owner@example.com")).thenReturn(owner);
		when(eventService.requireAccessibleEvent("event-id", owner)).thenReturn(event);
		when(eventService.roleFor(event, owner)).thenReturn(EventRole.OWNER);
		when(galleryRepository.existsBySlug(anyString())).thenReturn(false);
		when(galleryRepository.save(any(Gallery.class))).thenAnswer(invocation -> {
			Gallery gallery = invocation.getArgument(0);
			gallery.setId("gallery-id");
			gallery.setCreatedAt(LocalDateTime.now());
			gallery.setUpdatedAt(LocalDateTime.now());
			return gallery;
		});
		doThrow(new IllegalStateException("audit unavailable")).when(auditService)
				.logRequiredGalleryEvent(eq("owner@example.com"), any(), eq("event-id"), eq("gallery-id"), anyString());

		assertThatThrownBy(
				() -> service.create("event-id", new GalleryWriteRequest("Reception", null, 10), "owner@example.com"))
				.isInstanceOf(IllegalStateException.class).hasMessage("audit unavailable");
	}
}
