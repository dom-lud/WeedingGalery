package pl.backend.weddinggallery.publicaccess.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.backend.weddinggallery.publicaccess.dto.PublicAccessRequest;
import pl.backend.weddinggallery.publicaccess.dto.PublicGalleryResponse;
import pl.backend.weddinggallery.publicaccess.service.GalleryAccessService;
import pl.backend.weddinggallery.publicaccess.service.PublicRateLimiter;

@RestController
@RequestMapping("/api/public/galleries/{slug}")
@RequiredArgsConstructor
public class PublicGalleryController {
	private final GalleryAccessService service;
	private final PublicRateLimiter rateLimiter;
	@PostMapping("/access")
	public PublicGalleryResponse access(@PathVariable String slug, @Valid @RequestBody PublicAccessRequest body,
			HttpServletRequest request, HttpSession session) {
		rateLimiter.check(clientAddress(request) + ":access:" + slug);
		return service.exchange(slug, body, session);
	}
	@GetMapping
	public PublicGalleryResponse get(@PathVariable String slug, HttpSession session) {
		return service.getPublic(slug, session);
	}
	private String clientAddress(HttpServletRequest request) {
		String proxied = request.getHeader("X-Real-IP");
		return proxied == null || proxied.isBlank() ? request.getRemoteAddr() : proxied;
	}
}
