package pl.backend.weddinggallery.gallery.controller;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.backend.weddinggallery.gallery.dto.*;
import pl.backend.weddinggallery.gallery.service.GalleryCustomizationService;
@RestController
@RequestMapping("/api/galleries/{galleryId}/customization")
@RequiredArgsConstructor
public class GalleryCustomizationController {
	private final GalleryCustomizationService service;
	@GetMapping
	public GalleryCustomizationResponse get(@PathVariable String galleryId, Principal p) {
		return service.get(galleryId, p.getName());
	}
	@PutMapping
	public GalleryCustomizationResponse update(@PathVariable String galleryId,
			@Valid @RequestBody GalleryCustomizationRequest r, Principal p) {
		return service.update(galleryId, r, p.getName());
	}
}
