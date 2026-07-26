package pl.backend.weddinggallery.media.controller;

import java.security.Principal;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.media.dto.*;
import pl.backend.weddinggallery.media.exception.MediaModerationErrorCode;
import pl.backend.weddinggallery.media.service.MediaModerationService;

@RestController
@RequestMapping("/api/events/{eventId}/galleries/{galleryId}/media")
@RequiredArgsConstructor
public class MediaModerationController {
	private final MediaModerationService moderationService;

	@PostMapping("/{mediaId}/approve")
	public MediaModerationResponse approve(@PathVariable String eventId, @PathVariable String galleryId,
			@PathVariable String mediaId, @Valid @RequestBody(required = false) MediaModerationRequest request,
			Principal principal) {
		return moderate(eventId, galleryId, mediaId, MediaModerationAction.APPROVE, request, principal);
	}

	@PostMapping("/{mediaId}/reject")
	public MediaModerationResponse reject(@PathVariable String eventId, @PathVariable String galleryId,
			@PathVariable String mediaId, @Valid @RequestBody MediaModerationRequest request, Principal principal) {
		return moderate(eventId, galleryId, mediaId, MediaModerationAction.REJECT, request, principal);
	}

	@PostMapping("/{mediaId}/hide")
	public MediaModerationResponse hide(@PathVariable String eventId, @PathVariable String galleryId,
			@PathVariable String mediaId, @Valid @RequestBody MediaModerationRequest request, Principal principal) {
		return moderate(eventId, galleryId, mediaId, MediaModerationAction.HIDE, request, principal);
	}

	@PostMapping("/{mediaId}/restore")
	public MediaModerationResponse restore(@PathVariable String eventId, @PathVariable String galleryId,
			@PathVariable String mediaId, @Valid @RequestBody(required = false) MediaModerationRequest request,
			Principal principal) {
		return moderate(eventId, galleryId, mediaId, MediaModerationAction.RESTORE, request, principal);
	}

	@PostMapping("/bulk-actions")
	public List<MediaModerationResponse> bulk(@PathVariable String eventId, @PathVariable String galleryId,
			@Valid @RequestBody BulkMediaModerationRequest request, Principal principal) {
		return moderationService.bulk(eventId, galleryId, request, principal.getName());
	}

	private MediaModerationResponse moderate(String eventId, String galleryId, String mediaId,
			MediaModerationAction action, MediaModerationRequest request, Principal principal) {
		String reason = request == null ? null : request.reason();
		if ((action == MediaModerationAction.REJECT || action == MediaModerationAction.HIDE)
				&& (reason == null || reason.isBlank()))
			throw new AppException(MediaModerationErrorCode.MEDIA_MODERATION_REASON_REQUIRED);
		return moderationService.moderate(eventId, galleryId, mediaId, action, reason, principal.getName());
	}
}
