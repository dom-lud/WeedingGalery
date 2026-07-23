package pl.backend.weddinggallery.media.controller;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import pl.backend.weddinggallery.media.dto.*;
import pl.backend.weddinggallery.media.service.MediaGalleryService;

@RestController
@RequestMapping("/api/events/{eventId}/galleries/{galleryId}")
@RequiredArgsConstructor
public class MediaGalleryController {
	private final MediaGalleryService mediaService;

	@GetMapping("/media")
	public List<MediaItemResponse> list(@PathVariable String eventId, @PathVariable String galleryId,
			Principal principal) {
		return mediaService.managedMedia(eventId, galleryId, principal.getName());
	}

	@GetMapping("/media/{mediaId}/thumbnail")
	public ResponseEntity<InputStreamResource> thumbnail(@PathVariable String eventId, @PathVariable String galleryId,
			@PathVariable String mediaId, Principal principal) {
		return stream(mediaService.managedResource(eventId, galleryId, mediaId, true, principal.getName()), false);
	}

	@GetMapping("/media/{mediaId}/content")
	public ResponseEntity<InputStreamResource> content(@PathVariable String eventId, @PathVariable String galleryId,
			@PathVariable String mediaId, Principal principal) {
		return stream(mediaService.managedResource(eventId, galleryId, mediaId, false, principal.getName()), false);
	}

	@GetMapping("/download")
	public ResponseEntity<StreamingResponseBody> download(@PathVariable String eventId, @PathVariable String galleryId,
			Principal principal) {
		GalleryDownload download = mediaService.ownerDownload(eventId, galleryId, principal.getName());
		ContentDisposition disposition = ContentDisposition.attachment()
				.filename(download.fileName(), StandardCharsets.UTF_8).build();
		StreamingResponseBody body = output -> {
			try (ZipOutputStream zip = new ZipOutputStream(output)) {
				for (MediaResource file : download.files()) {
					zip.putNextEntry(new ZipEntry(file.fileName()));
					try (var input = mediaService.open(file.objectKey())) {
						input.transferTo(zip);
					}
					zip.closeEntry();
				}
			}
		};
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/zip"))
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.header("X-Content-Type-Options", "nosniff").cacheControl(CacheControl.noStore()).body(body);
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
