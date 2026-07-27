package pl.backend.weddinggallery.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.backend.weddinggallery.admin.dto.*;
import pl.backend.weddinggallery.admin.service.AdminService;
import pl.backend.weddinggallery.audit.dto.AuditFilter;
import pl.backend.weddinggallery.audit.model.EventType;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
	private final AdminService adminService;

	@GetMapping("/dashboard")
	public ResponseEntity<?> dashboard(Authentication authentication) {
		if (!adminService.isAdmin(authentication))
			return forbidden();
		return ResponseEntity.ok(adminService.dashboard());
	}

	@GetMapping("/users")
	public ResponseEntity<?> users(Authentication authentication, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		if (!adminService.isAdmin(authentication))
			return forbidden();
		return ResponseEntity.ok(adminService.users(pageRequest(page, size)));
	}

	@GetMapping("/events")
	public ResponseEntity<?> events(Authentication authentication, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		if (!adminService.isAdmin(authentication))
			return forbidden();
		return ResponseEntity.ok(adminService.events(pageRequest(page, size)));
	}

	@GetMapping("/galleries")
	public ResponseEntity<?> galleries(Authentication authentication, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		if (!adminService.isAdmin(authentication))
			return forbidden();
		return ResponseEntity.ok(adminService.galleries(pageRequest(page, size)));
	}

	@GetMapping("/media")
	public ResponseEntity<?> media(Authentication authentication, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		if (!adminService.isAdmin(authentication))
			return forbidden();
		return ResponseEntity.ok(adminService.media(pageRequest(page, size)));
	}

	@GetMapping("/audit")
	public ResponseEntity<?> audit(Authentication authentication, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size, @RequestParam(required = false) EventType eventType,
			@RequestParam(required = false) String actorType, @RequestParam(required = false) String eventId,
			@RequestParam(required = false) String galleryId, @RequestParam(required = false) LocalDateTime from,
			@RequestParam(required = false) LocalDateTime to) {
		if (!adminService.isAdmin(authentication))
			return forbidden();
		return ResponseEntity.ok(adminService.audit(pageRequest(page, size),
				new AuditFilter(eventType, actorType, eventId, galleryId, from, to)));
	}

	// Kept as a small Java-level compatibility overload for existing boundary
	// tests.
	public ResponseEntity<?> audit(Authentication authentication, int page, int size) {
		if (!adminService.isAdmin(authentication))
			return forbidden();
		return ResponseEntity.ok(adminService.audit(pageRequest(page, size)));
	}

	@GetMapping("/alerts")
	public ResponseEntity<?> alerts(Authentication authentication, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		if (!adminService.isAdmin(authentication))
			return forbidden();
		return ResponseEntity.ok(adminService.alerts(pageRequest(page, size)));
	}

	@PostMapping("/alerts/{alertId}/acknowledge")
	public ResponseEntity<?> acknowledgeAlert(Authentication authentication, @PathVariable String alertId) {
		if (!adminService.isAdmin(authentication))
			return forbidden();
		return ResponseEntity.ok(adminService.acknowledgeAlert(authentication.getName(), alertId));
	}

	@GetMapping("/statistics")
	public ResponseEntity<?> statistics(Authentication authentication) {
		if (!adminService.isAdmin(authentication))
			return forbidden();
		return ResponseEntity.ok(adminService.statistics());
	}

	@PostMapping("/users/{userId}/lock")
	public ResponseEntity<?> lock(Authentication authentication, @PathVariable String userId) {
		if (!adminService.isAdmin(authentication))
			return forbidden();
		return ResponseEntity.ok(adminService.lockUser(authentication.getName(), userId));
	}

	@PostMapping("/users/{userId}/unlock")
	public ResponseEntity<?> unlock(Authentication authentication, @PathVariable String userId) {
		if (!adminService.isAdmin(authentication))
			return forbidden();
		return ResponseEntity.ok(adminService.unlockUser(authentication.getName(), userId));
	}

	@PostMapping("/events/{eventId}/archive")
	public ResponseEntity<?> archive(Authentication authentication, @PathVariable String eventId) {
		if (!adminService.isAdmin(authentication))
			return forbidden();
		return ResponseEntity.ok(adminService.archiveEvent(authentication.getName(), eventId));
	}

	@PostMapping("/media/{mediaId}/hide")
	public ResponseEntity<?> hide(Authentication authentication, @PathVariable String mediaId) {
		if (!adminService.isAdmin(authentication))
			return forbidden();
		return ResponseEntity.ok(adminService.hideMedia(authentication.getName(), mediaId));
	}

	@ExceptionHandler(AdminService.AdminNotFoundException.class)
	ResponseEntity<Void> notFound() {
		return ResponseEntity.notFound().build();
	}

	private PageRequest pageRequest(int page, int size) {
		if (page < 0 || size < 1 || size > AdminService.maxPageSize()) {
			throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Invalid pagination");
		}
		return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
	}

	private ResponseEntity<Void> forbidden() {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}
}
