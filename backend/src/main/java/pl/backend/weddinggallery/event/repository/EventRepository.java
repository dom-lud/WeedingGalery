package pl.backend.weddinggallery.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.backend.weddinggallery.event.model.Event;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
	List<Event> findByOwnerId(String ownerId);
}
