package pl.backend.weddinggallery.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.backend.weddinggallery.event.service.EventService;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.statistics.model.UsageEvent;
import pl.backend.weddinggallery.statistics.model.UsageEventType;
import pl.backend.weddinggallery.statistics.repository.UsageEventRepository;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {
	@Mock
	private EntityManager entityManager;
	@Mock
	private EventService eventService;
	@Mock
	private GalleryRepository galleryRepository;
	@Mock
	private UsageEventRepository usageEventRepository;
	@Mock
	private TypedQuery<Long> query;
	@InjectMocks
	private StatisticsService service;

	@Test
	void aggregatesGlobalCountersAndRecordsUsage() {
		when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.getSingleResult()).thenReturn(10L, 6L, 4L, 2L, 2048L, 1L, 12L, 3L);

		var response = service.global();

		assertThat(response.mediaCount()).isEqualTo(10);
		assertThat(response.storageUsedBytes()).isEqualTo(2048);
		assertThat(response.publicViewCount()).isEqualTo(12);
		service.record(UsageEventType.GALLERY_VIEW, "event-1", "gallery-1", null, "access-1", 2);
		verify(usageEventRepository).save(any(UsageEvent.class));
	}

	@Test
	void aggregatesEventAndGalleryScopesAfterAccessChecks() {
		when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
		when(query.getSingleResult()).thenReturn(1L);
		Event event = Event.builder().id("event-1").build();
		when(eventService.requireUser("owner@example.com")).thenReturn(User.builder().id("u1").build());
		when(eventService.requireAccessibleEvent(eq("event-1"), any(User.class))).thenReturn(event);
		assertThat(service.forEvent("event-1", "owner@example.com").eventId()).isEqualTo("event-1");

		Gallery gallery = Gallery.builder().id("gallery-1").event(event).build();
		when(galleryRepository.findByIdAndEventIdAndDeletedAtIsNull("gallery-1", "event-1"))
				.thenReturn(java.util.Optional.of(gallery));
		assertThat(service.forGallery("event-1", "gallery-1", "owner@example.com").galleryId())
				.isEqualTo("gallery-1");
	}

	@Test
	void rejectsInvalidUsageEvent() {
		assertThatThrownBy(() -> service.record(null, "event", null, null, null, 1))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> service.record(UsageEventType.MEDIA_DOWNLOAD, "event", null, null, null, 0))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
