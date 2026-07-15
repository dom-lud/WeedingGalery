package pl.backend.weddinggallery.upload.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.backend.weddinggallery.publicaccess.service.PublicRateLimiter;
import pl.backend.weddinggallery.upload.dto.*;
import pl.backend.weddinggallery.upload.service.UploadService;

@RestController
@RequestMapping("/api/public/galleries/{slug}/upload-sessions")
@RequiredArgsConstructor
public class PublicUploadController {
	private final UploadService service;
	private final PublicRateLimiter rateLimiter;

	@PostMapping
	public ResponseEntity<UploadSessionResponse> create(@PathVariable String slug,
			@RequestHeader("Idempotency-Key") String idempotencyKey, @Valid @RequestBody UploadManifestRequest request,
			HttpSession session, HttpServletRequest httpRequest) {
		rateLimiter.check(clientAddress(httpRequest) + ":upload-session:" + slug, 30);
		UploadService.CreateResult result = service.create(slug, idempotencyKey, request, session);
		if (!result.created())
			return ResponseEntity.ok(result.response());
		return ResponseEntity
				.created(URI.create("/api/public/galleries/" + slug + "/upload-sessions/" + result.response().id()))
				.body(result.response());
	}

	@GetMapping("/{sessionId}")
	public UploadSessionResponse get(@PathVariable String slug, @PathVariable String sessionId, HttpSession session) {
		return service.get(slug, sessionId, session);
	}

	@PutMapping(path = "/{sessionId}/files/{clientFileId}", consumes = "multipart/form-data")
	public UploadFileResponse upload(@PathVariable String slug, @PathVariable String sessionId,
			@PathVariable String clientFileId, @RequestPart("file") MultipartFile file, HttpSession session,
			HttpServletRequest httpRequest) {
		return service.upload(slug, sessionId, clientFileId, file, session);
	}

	@PostMapping("/{sessionId}/cancel")
	public UploadSessionResponse cancel(@PathVariable String slug, @PathVariable String sessionId,
			HttpSession session) {
		return service.cancel(slug, sessionId, session);
	}

	private String clientAddress(HttpServletRequest request) {
		String proxied = request.getHeader("X-Real-IP");
		return proxied == null || proxied.isBlank() ? request.getRemoteAddr() : proxied;
	}
}
