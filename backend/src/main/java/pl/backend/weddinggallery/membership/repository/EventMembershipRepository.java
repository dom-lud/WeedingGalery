package pl.backend.weddinggallery.membership.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.backend.weddinggallery.membership.model.EventMembership;

public interface EventMembershipRepository extends JpaRepository<EventMembership, String> {
	List<EventMembership> findByEventIdAndRemovedAtIsNullOrderByJoinedAtAsc(String eventId);

	Optional<EventMembership> findByEventIdAndUserId(String eventId, String userId);

	Optional<EventMembership> findByIdAndEventIdAndRemovedAtIsNull(String id, String eventId);

	boolean existsByEventIdAndUserIdAndRemovedAtIsNull(String eventId, String userId);
}
