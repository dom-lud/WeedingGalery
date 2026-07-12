package pl.backend.weddinggallery.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.backend.weddinggallery.audit.model.AuditEvent;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
}
