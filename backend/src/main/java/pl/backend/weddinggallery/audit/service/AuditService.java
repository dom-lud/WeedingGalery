package pl.backend.weddinggallery.audit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import pl.backend.weddinggallery.audit.model.AuditEvent;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.repository.AuditEventRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

	private final AuditEventRepository auditEventRepository;
	private final PlatformTransactionManager transactionManager;

	public void logEvent(String userEmail, EventType eventType, String details) {
		try {
			TransactionTemplate transaction = new TransactionTemplate(transactionManager);
			transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			transaction.executeWithoutResult(status -> {
				AuditEvent event = new AuditEvent(eventType, userEmail, details);
				auditEventRepository.saveAndFlush(event);
			});
			log.debug("Audit event saved: {} for user: {}", eventType, userEmail);
		} catch (Exception e) {
			log.error("Failed to save audit event: [{}] for user: {}", eventType, userEmail, e);
		}
	}

	@Transactional
	public void logRequiredIdentityEvent(String userEmail, EventType eventType, String details) {
		AuditEvent event = new AuditEvent(eventType, userEmail, details);
		auditEventRepository.save(event);
		log.debug("Required identity audit event saved: {} for user: {}", eventType, userEmail);
	}

	@Transactional
	public void logRequiredEvent(String userEmail, EventType eventType, String eventId, String targetUserId,
			String details) {
		AuditEvent event = new AuditEvent(eventType, userEmail, eventId, targetUserId, details);
		auditEventRepository.save(event);
		log.debug("Required audit event saved: {} for event: {}", eventType, eventId);
	}

	@Transactional
	public void logRequiredGalleryEvent(String userEmail, EventType eventType, String eventId, String galleryId,
			String details) {
		AuditEvent event = AuditEvent.galleryEvent(eventType, userEmail, eventId, galleryId, details);
		auditEventRepository.save(event);
		log.debug("Required audit event saved: {} for gallery: {}", eventType, galleryId);
	}

	@Transactional
	public void logRequiredGuestGalleryEvent(String publicAccessId, EventType eventType, String eventId,
			String galleryId, String details) {
		AuditEvent event = AuditEvent.guestGalleryEvent(eventType, publicAccessId, eventId, galleryId, details);
		auditEventRepository.save(event);
		log.debug("Required guest audit event saved: {} for gallery: {}", eventType, galleryId);
	}
}
