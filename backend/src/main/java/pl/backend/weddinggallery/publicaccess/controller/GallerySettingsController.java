package pl.backend.weddinggallery.publicaccess.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.backend.weddinggallery.publicaccess.dto.*;
import pl.backend.weddinggallery.publicaccess.service.GalleryAccessService;

@RestController
@RequestMapping("/api/events/{eventId}/galleries/{galleryId}")
@RequiredArgsConstructor
public class GallerySettingsController {
	private final GalleryAccessService service;
	@GetMapping("/settings")
	public GallerySettingsResponse get(@PathVariable String eventId, @PathVariable String galleryId,
			Principal principal) {
		return service.getSettings(eventId, galleryId, principal.getName());
	}
	@PutMapping("/settings")
	public GallerySettingsResponse update(@PathVariable String eventId, @PathVariable String galleryId,
			@Valid @RequestBody GallerySettingsRequest request, Principal principal) {
		return service.updateSettings(eventId, galleryId, request, principal.getName());
	}
	@PostMapping("/access-token/rotate")
	public ResponseEntity<AccessTokenResponse> rotate(@PathVariable String eventId, @PathVariable String galleryId,
			Principal principal) {
		AccessTokenResponse response = service.rotateToken(eventId, galleryId, principal.getName());
		return ResponseEntity.created(URI.create("/api/events/" + eventId + "/galleries/" + galleryId + "/settings"))
				.body(response);
	}
	@PutMapping("/access-code")
	public ResponseEntity<Void> setCode(@PathVariable String eventId, @PathVariable String galleryId,
			@Valid @RequestBody AccessCodeRequest request, Principal principal) {
		service.setAccessCode(eventId, galleryId, request.accessCode(), principal.getName());
		return ResponseEntity.noContent().build();
	}
	@DeleteMapping("/access-code")
	public ResponseEntity<Void> clearCode(@PathVariable String eventId, @PathVariable String galleryId,
			Principal principal) {
		service.clearAccessCode(eventId, galleryId, principal.getName());
		return ResponseEntity.noContent().build();
	}
}
