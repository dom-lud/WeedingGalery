package pl.backend.weddinggallery.membership.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.backend.weddinggallery.event.dto.EventResponse;
import pl.backend.weddinggallery.membership.dto.AddEventManagerRequest;
import pl.backend.weddinggallery.membership.dto.EventMemberResponse;
import pl.backend.weddinggallery.membership.dto.OwnershipTransferRequest;
import pl.backend.weddinggallery.membership.service.EventMembershipService;

@RestController
@RequestMapping("/api/events/{eventId}")
@RequiredArgsConstructor
public class EventMembershipController {
	private final EventMembershipService membershipService;

	@GetMapping("/members")
	public List<EventMemberResponse> list(@PathVariable String eventId, Principal principal) {
		return membershipService.list(eventId, principal.getName());
	}

	@PostMapping("/members")
	public ResponseEntity<EventMemberResponse> add(@PathVariable String eventId,
			@Valid @RequestBody AddEventManagerRequest request, Principal principal) {
		EventMemberResponse response = membershipService.add(eventId, request, principal.getName());
		return ResponseEntity.created(URI.create("/api/events/" + eventId + "/members/" + response.id()))
				.body(response);
	}

	@DeleteMapping("/members/{membershipId}")
	public ResponseEntity<Void> remove(@PathVariable String eventId, @PathVariable String membershipId,
			Principal principal) {
		membershipService.remove(eventId, membershipId, principal.getName());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/ownership-transfer")
	public EventResponse transferOwnership(@PathVariable String eventId,
			@Valid @RequestBody OwnershipTransferRequest request, Principal principal) {
		return membershipService.transferOwnership(eventId, request, principal.getName());
	}
}
