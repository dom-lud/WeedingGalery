package pl.backend.weddinggallery.event.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.backend.weddinggallery.event.dto.EventResponse;
import pl.backend.weddinggallery.event.dto.EventWriteRequest;
import pl.backend.weddinggallery.event.service.EventService;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
	private final EventService eventService;

	@PostMapping
	public ResponseEntity<EventResponse> create(@Valid @RequestBody EventWriteRequest request, Principal principal) {
		EventResponse response = eventService.create(request, principal.getName());
		return ResponseEntity.created(URI.create("/api/events/" + response.id())).body(response);
	}

	@GetMapping
	public List<EventResponse> list(Principal principal) {
		return eventService.list(principal.getName());
	}

	@GetMapping("/{eventId}")
	public EventResponse get(@PathVariable String eventId, Principal principal) {
		return eventService.get(eventId, principal.getName());
	}

	@PutMapping("/{eventId}")
	public EventResponse update(@PathVariable String eventId, @Valid @RequestBody EventWriteRequest request,
			Principal principal) {
		return eventService.update(eventId, request, principal.getName());
	}

	@PostMapping("/{eventId}/archive")
	public EventResponse archive(@PathVariable String eventId, Principal principal) {
		return eventService.archive(eventId, principal.getName());
	}

	@DeleteMapping("/{eventId}")
	public ResponseEntity<Void> delete(@PathVariable String eventId, Principal principal) {
		eventService.delete(eventId, principal.getName());
		return ResponseEntity.noContent().build();
	}
}
