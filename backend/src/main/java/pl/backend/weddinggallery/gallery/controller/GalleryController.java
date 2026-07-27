package pl.backend.weddinggallery.gallery.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.backend.weddinggallery.gallery.dto.GalleryResponse;
import pl.backend.weddinggallery.gallery.dto.GalleryWriteRequest;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.qr.exception.QrErrorCode;
import pl.backend.weddinggallery.gallery.service.GalleryService;
import pl.backend.weddinggallery.qr.model.QrFormat;
import pl.backend.weddinggallery.qr.service.QrCodeService;

@RestController
@RequestMapping("/api/events/{eventId}/galleries")
@RequiredArgsConstructor
public class GalleryController {
	private final GalleryService galleryService;
	private final QrCodeService qrCodeService;

	@GetMapping
	public List<GalleryResponse> list(@PathVariable String eventId, Principal principal) {
		return galleryService.list(eventId, principal.getName());
	}

	@PostMapping
	public ResponseEntity<GalleryResponse> create(@PathVariable String eventId,
			@Valid @RequestBody GalleryWriteRequest request, Principal principal) {
		GalleryResponse response = galleryService.create(eventId, request, principal.getName());
		return ResponseEntity.created(URI.create("/api/events/" + eventId + "/galleries/" + response.id()))
				.body(response);
	}

	@GetMapping("/{galleryId}")
	public GalleryResponse get(@PathVariable String eventId, @PathVariable String galleryId, Principal principal) {
		return galleryService.get(eventId, galleryId, principal.getName());
	}

	@GetMapping(value = "/{galleryId}/qr", produces = {MediaType.IMAGE_PNG_VALUE, "image/svg+xml"})
	public ResponseEntity<byte[]> qr(@PathVariable String eventId, @PathVariable String galleryId,
			@RequestParam(defaultValue = "PNG") String format, @RequestParam(defaultValue = "512") int size,
			Principal principal) {
		QrFormat qrFormat;
		try {
			qrFormat = QrFormat.valueOf(format.toUpperCase(java.util.Locale.ROOT));
		} catch (RuntimeException exception) {
			throw new AppException(QrErrorCode.QR_INVALID_FORMAT);
		}
		String slug = galleryService.publicSlug(eventId, galleryId, principal.getName());
		String publicUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/g/{slug}").buildAndExpand(slug).toUriString();
		var generated = qrCodeService.generate(publicUrl, qrFormat, size);
		return ResponseEntity.ok().contentType(generated.contentType()).cacheControl(CacheControl.noStore())
				.body(generated.content());
	}

	@PutMapping("/{galleryId}")
	public GalleryResponse update(@PathVariable String eventId, @PathVariable String galleryId,
			@Valid @RequestBody GalleryWriteRequest request, Principal principal) {
		return galleryService.update(eventId, galleryId, request, principal.getName());
	}

	@PostMapping("/{galleryId}/archive")
	public GalleryResponse archive(@PathVariable String eventId, @PathVariable String galleryId, Principal principal) {
		return galleryService.archive(eventId, galleryId, principal.getName());
	}

	@DeleteMapping("/{galleryId}")
	public ResponseEntity<Void> delete(@PathVariable String eventId, @PathVariable String galleryId,
			Principal principal) {
		galleryService.delete(eventId, galleryId, principal.getName());
		return ResponseEntity.noContent().build();
	}
}
