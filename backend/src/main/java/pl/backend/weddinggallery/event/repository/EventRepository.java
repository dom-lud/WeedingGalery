package pl.backend.weddinggallery.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.backend.weddinggallery.event.model.Event;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
	@Query("""
			select distinct e from Event e
			left join EventMembership m on m.event = e and m.user.id = :userId and m.removedAt is null
			where e.deletedAt is null and (e.owner.id = :userId or m.id is not null)
			order by e.createdAt desc
			""")
	List<Event> findAccessibleByUserId(@Param("userId") String userId);

	@Query("""
			select distinct e from Event e
			left join EventMembership m on m.event = e and m.user.id = :userId and m.removedAt is null
			where e.id = :eventId and e.deletedAt is null and (e.owner.id = :userId or m.id is not null)
			""")
	Optional<Event> findAccessibleById(@Param("eventId") String eventId, @Param("userId") String userId);
}
