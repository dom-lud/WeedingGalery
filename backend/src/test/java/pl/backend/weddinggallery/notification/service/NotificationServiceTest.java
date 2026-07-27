package pl.backend.weddinggallery.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import pl.backend.weddinggallery.notification.model.*;
import pl.backend.weddinggallery.notification.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
	@Mock
	private NotificationRepository repository;
	@InjectMocks
	private NotificationService service;

	@Test
	void createsAlertOnceAndReopensAcknowledgedDedupeKey() {
		Notification acknowledged = notification(NotificationStatus.ACKNOWLEDGED);
		when(repository.findByDedupeKey("processing:1")).thenReturn(java.util.Optional.of(acknowledged));

		var response = service.createAdminAlert("PROCESSING_FAILED", NotificationSeverity.CRITICAL, "processing:1",
				"Processing failed", "Retry required", "MEDIA", "media-1");

		assertThat(response.status()).isEqualTo(NotificationStatus.OPEN);
		assertThat(acknowledged.getAcknowledgedAt()).isNull();
		assertThat(acknowledged.getSeverity()).isEqualTo(NotificationSeverity.CRITICAL);
	}

	@Test
	void listsOpenAlertsAndAcknowledgesOnlyOnce() {
		Notification open = notification(NotificationStatus.OPEN);
		when(repository.findByAudienceAndStatus(NotificationAudience.ADMIN, NotificationStatus.OPEN,
				PageRequest.of(0, 10))).thenReturn(new PageImpl<>(java.util.List.of(open)));
		assertThat(service.adminOpen(PageRequest.of(0, 10))).hasSize(1);

		when(repository.findById("n1")).thenReturn(java.util.Optional.of(open));
		assertThat(service.acknowledge("n1", "admin@example.com").status()).isEqualTo(NotificationStatus.ACKNOWLEDGED);
		LocalDateTime acknowledgedAt = open.getAcknowledgedAt();
		service.acknowledge("n1", "other@example.com");
		assertThat(open.getAcknowledgedAt()).isEqualTo(acknowledgedAt);
	}

	@Test
	void rejectsUnknownAlert() {
		when(repository.findById("missing")).thenReturn(java.util.Optional.empty());
		assertThatThrownBy(() -> service.acknowledge("missing", "admin@example.com"))
				.isInstanceOf(NotificationService.NotificationNotFoundException.class);
	}

	private Notification notification(NotificationStatus status) {
		return Notification.builder().id("n1").audience(NotificationAudience.ADMIN).notificationType("TYPE")
				.severity(NotificationSeverity.WARNING).status(status).dedupeKey("key").title("Title")
				.message("Message").createdAt(LocalDateTime.now()).build();
	}
}
