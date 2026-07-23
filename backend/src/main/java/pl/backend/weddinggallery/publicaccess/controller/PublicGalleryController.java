package pl.backend.weddinggallery.publicaccess.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pl.backend.weddinggallery.media.dto.MediaResource;
import pl.backend.weddinggallery.media.service.MediaGalleryService;
import pl.backend.weddinggallery.publicaccess.dto.PublicAccessRequest;
import pl.backend.weddinggallery.publicaccess.dto.PublicGalleryResponse;
import pl.backend.weddinggallery.publicaccess.service.GalleryAccessService;
import pl.backend.weddinggallery.publicaccess.service.PublicRateLimiter;

@RestController
@RequestMapping("/api/public/galleries/{slug}")
@RequiredArgsConstructor
public class PublicGalleryController {
	private final GalleryAccessService service;
	private final MediaGalleryService mediaService;
	private final PublicRateLimiter rateLimiter;
	@PostMapping("/access")
	public PublicGalleryResponse access(@PathVariable String slug, @Valid @RequestBody PublicAccessRequest body,
			HttpServletRequest request, HttpSession session) {
		String address = clientAddress(request);
		rateLimiter.check(address + ":access-global", 30);
		rateLimiter.check(address + ":access:" + slug);
		return service.exchange(slug, body, session);
	}
	@GetMapping
	public PublicGalleryResponse get(@PathVariable String slug, HttpSession session) {
		return service.getPublic(slug, session);
	}

	@GetMapping("/media/{mediaId}/thumbnail")
	public ResponseEntity<InputStreamResource> thumbnail(@PathVariable String slug, @PathVariable String mediaId,
			HttpSession session) {
		return stream(mediaService.publicResource(slug, mediaId, true, session), false);
	}

	@GetMapping("/media/{mediaId}/content")
	public ResponseEntity<InputStreamResource> content(@PathVariable String slug, @PathVariable String mediaId,
			HttpSession session) {
		return stream(mediaService.publicResource(slug, mediaId, false, session), false);
	}

	private String clientAddress(HttpServletRequest request) {
		String proxied = request.getHeader("X-Real-IP");
		return proxied == null || proxied.isBlank() ? request.getRemoteAddr() : proxied;
	}

	private ResponseEntity<InputStreamResource> stream(MediaResource resource, boolean attachment) {
		ContentDisposition disposition = (attachment ? ContentDisposition.attachment() : ContentDisposition.inline())
				.filename(resource.fileName(), StandardCharsets.UTF_8).build();
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(resource.contentType()))
				.contentLength(resource.size()).header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.header("X-Content-Type-Options", "nosniff").cacheControl(CacheControl.noStore())
				.body(new InputStreamResource(mediaService.open(resource.objectKey())));
	}
}
