package pl.backend.weddinggallery.media.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.event.service.EventService;
import pl.backend.weddinggallery.gallery.exception.GalleryErrorCode;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.media.dto.*;
import pl.backend.weddinggallery.media.exception.MediaModerationErrorCode;
import pl.backend.weddinggallery.media.model.MediaFile;
import pl.backend.weddinggallery.media.model.PublicationStatus;
import pl.backend.weddinggallery.media.repository.MediaFileRepository;
import pl.backend.weddinggallery.user.model.User;

@Service
@RequiredArgsConstructor
public class MediaModerationService {
	private static final int MAX_BULK_ITEMS = 100;
	private final MediaFileRepository mediaFiles;
	private final GalleryRepository galleries;
	private final EventService eventService;
	private final AuditService auditService;

	@Transactional
	public MediaModerationResponse moderate(String eventId, String galleryId, String mediaId,
			MediaModerationAction action, String actorEmail) {
		return moderate(eventId, galleryId, mediaId, action, null, actorEmail);
	}

	@Transactional
	public MediaModerationResponse moderate(String eventId, String galleryId, String mediaId,
			MediaModerationAction action, String reason, String actorEmail) {
		ModerationContext context = requireContext(eventId, galleryId, actorEmail);
		MediaFile media = mediaFiles.findByIdAndGalleryId(mediaId, galleryId)
				.orElseThrow(() -> new AppException(MediaModerationErrorCode.MEDIA_MODERATION_NOT_FOUND));
		validateReason(action, reason);
		return apply(media, context, action);
	}

	@Transactional
	public List<MediaModerationResponse> bulk(String eventId, String galleryId, BulkMediaModerationRequest request,
			String actorEmail) {
		if (request == null || request.mediaIds() == null || request.mediaIds().isEmpty())
			throw new AppException(MediaModerationErrorCode.MEDIA_MODERATION_EMPTY_BULK);
		if (request.mediaIds().size() > MAX_BULK_ITEMS)
			throw new AppException(MediaModerationErrorCode.MEDIA_MODERATION_BULK_LIMIT_EXCEEDED);
		if (request.mediaIds().stream().distinct().count() != request.mediaIds().size())
			throw new AppException(MediaModerationErrorCode.MEDIA_MODERATION_DUPLICATE_MEDIA);
		validateReason(request.action(), request.reason());
		ModerationContext context = requireContext(eventId, galleryId, actorEmail);
		List<MediaModerationResponse> result = new ArrayList<>();
		for (String mediaId : request.mediaIds()) {
			MediaFile media = mediaFiles.findByIdAndGalleryId(mediaId, galleryId)
					.orElseThrow(() -> new AppException(MediaModerationErrorCode.MEDIA_MODERATION_NOT_FOUND));
			result.add(apply(media, context, request.action()));
		}
		return result;
	}

	private MediaModerationResponse apply(MediaFile media, ModerationContext context, MediaModerationAction action) {
		PublicationStatus target = target(action);
		PublicationStatus previous = media.getPublicationStatus();
		if (!isAllowedTransition(previous, action, target))
			throw new AppException(MediaModerationErrorCode.MEDIA_MODERATION_INVALID_TRANSITION);
		if (previous != target) {
			media.setPublicationStatus(target);
			auditService.logRequiredGalleryEvent(context.actor().getEmail(), auditType(action), context.event().getId(),
					context.gallery().getId(), "mediaId=" + media.getId() + ",from=" + previous + ",to=" + target);
		}
		return new MediaModerationResponse(media.getId(), target, previous != target);
	}

	private boolean isAllowedTransition(PublicationStatus previous, MediaModerationAction action,
			PublicationStatus target) {
		if (previous == target)
			return true;
		return switch (action) {
			case APPROVE, REJECT -> previous == PublicationStatus.PENDING;
			case HIDE -> previous == PublicationStatus.APPROVED;
			case RESTORE -> previous == PublicationStatus.HIDDEN || previous == PublicationStatus.REJECTED;
		};
	}

	private void validateReason(MediaModerationAction action, String reason) {
		if ((action == MediaModerationAction.REJECT || action == MediaModerationAction.HIDE)
				&& (reason == null || reason.isBlank()))
			throw new AppException(MediaModerationErrorCode.MEDIA_MODERATION_REASON_REQUIRED);
	}

	private ModerationContext requireContext(String eventId, String galleryId, String actorEmail) {
		User actor = eventService.requireUser(actorEmail);
		Event event = eventService.requireAccessibleEvent(eventId, actor);
		Gallery gallery = galleries.findByIdAndEventIdAndDeletedAtIsNull(galleryId, eventId)
				.orElseThrow(() -> new AppException(GalleryErrorCode.GALLERY_NOT_FOUND));
		return new ModerationContext(actor, event, gallery);
	}

	private PublicationStatus target(MediaModerationAction action) {
		return switch (action) {
			case APPROVE -> PublicationStatus.APPROVED;
			case REJECT -> PublicationStatus.REJECTED;
			case HIDE -> PublicationStatus.HIDDEN;
			case RESTORE -> PublicationStatus.APPROVED;
		};
	}

	private EventType auditType(MediaModerationAction action) {
		return switch (action) {
			case APPROVE -> EventType.MEDIA_PUBLICATION_APPROVED;
			case REJECT -> EventType.MEDIA_PUBLICATION_REJECTED;
			case HIDE -> EventType.MEDIA_PUBLICATION_HIDDEN;
			case RESTORE -> EventType.MEDIA_PUBLICATION_RESTORED;
		};
	}

	private record ModerationContext(User actor, Event event, Gallery gallery) {
	}
}
