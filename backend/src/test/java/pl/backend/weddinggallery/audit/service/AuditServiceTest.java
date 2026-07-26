package pl.backend.weddinggallery.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import pl.backend.weddinggallery.audit.model.AuditEvent;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.repository.AuditEventRepository;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

	@Mock
	private AuditEventRepository auditEventRepository;

	@Mock
	private PlatformTransactionManager transactionManager;

	private AuditService service;

	@BeforeEach
	void setUp() {
		service = new AuditService(auditEventRepository, transactionManager);
	}

	@Test
	void logRequiredIdentityEventSavesIdentityAuditEvent() {
		service.logRequiredIdentityEvent("user@example.com", EventType.USER_REGISTERED, "registered");

		AuditEvent event = savedEvent();
		assertThat(event.getEventType()).isEqualTo(EventType.USER_REGISTERED);
		assertThat(event.getUserEmail()).isEqualTo("user@example.com");
		assertThat(event.getDetails()).isEqualTo("registered");
		assertThat(event.getActorType()).isEqualTo("USER");
	}

	@Test
	void logRequiredEventSavesEventAndTargetUserReferences() {
		service.logRequiredEvent("admin@example.com", EventType.EVENT_MANAGER_ADDED, "event-1", "user-2",
				"manager added");

		AuditEvent event = savedEvent();
		assertThat(event.getEventType()).isEqualTo(EventType.EVENT_MANAGER_ADDED);
		assertThat(event.getUserEmail()).isEqualTo("admin@example.com");
		assertThat(event.getEventId()).isEqualTo("event-1");
		assertThat(event.getTargetUserId()).isEqualTo("user-2");
		assertThat(event.getDetails()).isEqualTo("manager added");
	}

	@Test
	void logRequiredGalleryEventSavesGalleryReferences() {
		service.logRequiredGalleryEvent("owner@example.com", EventType.GALLERY_CREATED, "event-1", "gallery-1",
				"created");

		AuditEvent event = savedEvent();
		assertThat(event.getEventType()).isEqualTo(EventType.GALLERY_CREATED);
		assertThat(event.getUserEmail()).isEqualTo("owner@example.com");
		assertThat(event.getEventId()).isEqualTo("event-1");
		assertThat(event.getGalleryId()).isEqualTo("gallery-1");
		assertThat(event.getActorType()).isEqualTo("USER");
	}

	@Test
	void logRequiredGuestGalleryEventSavesGuestReferenceAndActorType() {
		service.logRequiredGuestGalleryEvent("access-1", EventType.MEDIA_STORED, "event-1", "gallery-1", "stored");

		AuditEvent event = savedEvent();
		assertThat(event.getEventType()).isEqualTo(EventType.MEDIA_STORED);
		assertThat(event.getPublicAccessId()).isEqualTo("access-1");
		assertThat(event.getEventId()).isEqualTo("event-1");
		assertThat(event.getGalleryId()).isEqualTo("gallery-1");
		assertThat(event.getActorType()).isEqualTo("GUEST");
	}

	@Test
	void logEventSavesUserAndDetailsWithRequiresNewPath() {
		service.logEvent("user@example.com", EventType.USER_LOGGED_IN, "logged in");

		AuditEvent event = flushedEvent();
		assertThat(event.getEventType()).isEqualTo(EventType.USER_LOGGED_IN);
		assertThat(event.getUserEmail()).isEqualTo("user@example.com");
		assertThat(event.getDetails()).isEqualTo("logged in");
		assertThat(event.getActorType()).isEqualTo("USER");
	}

	@Test
	void logSystemGalleryEventSavesSystemActorAndGalleryReferences() {
		service.logSystemGalleryEvent(EventType.MEDIA_PROCESSING_SUCCEEDED, "event-1", "gallery-1", "processed");

		AuditEvent event = flushedEvent();
		assertThat(event.getEventType()).isEqualTo(EventType.MEDIA_PROCESSING_SUCCEEDED);
		assertThat(event.getUserEmail()).isNull();
		assertThat(event.getActorType()).isEqualTo("SYSTEM");
		assertThat(event.getEventId()).isEqualTo("event-1");
		assertThat(event.getGalleryId()).isEqualTo("gallery-1");
		assertThat(event.getDetails()).isEqualTo("processed");
	}

	@Test
	void logEventDoesNotPropagateTransactionFailure() {
		when(transactionManager.getTransaction(any(TransactionDefinition.class)))
				.thenThrow(new IllegalStateException("audit database unavailable"));

		assertThatCode(() -> service.logEvent("user@example.com", EventType.USER_LOGGED_IN, "logged in"))
				.doesNotThrowAnyException();

		verify(auditEventRepository, never()).saveAndFlush(any(AuditEvent.class));
	}

	@Test
	void logSystemGalleryEventDoesNotPropagateTransactionFailure() {
		when(transactionManager.getTransaction(any(TransactionDefinition.class)))
				.thenThrow(new IllegalStateException("audit database unavailable"));

		assertThatCode(() -> service.logSystemGalleryEvent(EventType.MEDIA_PROCESSING_FAILED, "event-1", "gallery-1",
				"processing failed")).doesNotThrowAnyException();

		verify(auditEventRepository, never()).saveAndFlush(any(AuditEvent.class));
	}

	private AuditEvent savedEvent() {
		ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
		verify(auditEventRepository).save(captor.capture());
		return captor.getValue();
	}

	private AuditEvent flushedEvent() {
		ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
		verify(auditEventRepository).saveAndFlush(captor.capture());
		return captor.getValue();
	}
}
