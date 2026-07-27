package pl.backend.weddinggallery.statistics.repository;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.backend.weddinggallery.statistics.model.UsageEvent;
import pl.backend.weddinggallery.statistics.model.UsageEventType;

@Repository
public interface UsageEventRepository extends JpaRepository<UsageEvent, Long> {
	long countByEventIdAndEventType(String eventId, UsageEventType eventType);

	long countByGalleryIdAndEventType(String galleryId, UsageEventType eventType);

	long countByEventTypeAndOccurredAtBetween(UsageEventType eventType, LocalDateTime from, LocalDateTime to);
}
