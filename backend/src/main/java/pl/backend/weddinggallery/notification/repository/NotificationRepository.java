package pl.backend.weddinggallery.notification.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.backend.weddinggallery.notification.model.Notification;
import pl.backend.weddinggallery.notification.model.NotificationAudience;
import pl.backend.weddinggallery.notification.model.NotificationStatus;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
	Optional<Notification> findByDedupeKey(String dedupeKey);

	Page<Notification> findByAudienceAndStatus(NotificationAudience audience, NotificationStatus status,
			Pageable pageable);
}
