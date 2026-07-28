package pl.backend.weddinggallery.audit.repository;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.backend.weddinggallery.audit.model.AuditEvent;
import pl.backend.weddinggallery.audit.model.EventType;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
	@Query("""
			select a from AuditEvent a
			where (:eventType is null or a.eventType = :eventType)
			and (:actorType is null or a.actorType = :actorType)
			and (:eventId is null or a.eventId = :eventId)
			and (:galleryId is null or a.galleryId = :galleryId)
			and (:from is null or a.createdAt >= :from)
			and (:to is null or a.createdAt < :to)
			order by a.createdAt desc, a.id desc
			""")
	Page<AuditEvent> findFiltered(@Param("eventType") EventType eventType, @Param("actorType") String actorType,
			@Param("eventId") String eventId, @Param("galleryId") String galleryId, @Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to, Pageable pageable);
}
