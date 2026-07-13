package pl.backend.weddinggallery.audit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.backend.weddinggallery.audit.model.AuditEvent;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.repository.AuditEventRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

	private final AuditEventRepository auditEventRepository;

	@Transactional
	public void logEvent(String userEmail, EventType eventType, String details) {
		try {
			AuditEvent event = new AuditEvent(eventType, userEmail, details);
			auditEventRepository.save(event);
			log.debug("Audit event saved: {} for user: {}", eventType, userEmail);
		} catch (Exception e) {
			log.error("Failed to save audit event: [{}] for user: {}", eventType, userEmail, e);
		}
	}
}
