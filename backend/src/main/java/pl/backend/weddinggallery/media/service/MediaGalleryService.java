package pl.backend.weddinggallery.media.service;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.exception.EventErrorCode;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.event.service.EventService;
import pl.backend.weddinggallery.gallery.exception.GalleryErrorCode;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.media.dto.*;
import pl.backend.weddinggallery.media.model.*;
import pl.backend.weddinggallery.media.repository.*;
import pl.backend.weddinggallery.membership.model.EventRole;
import pl.backend.weddinggallery.publicaccess.service.GalleryAccessService;
import pl.backend.weddinggallery.storage.StorageService;
import pl.backend.weddinggallery.user.model.User;

@Service
@RequiredArgsConstructor
public class MediaGalleryService {
	private static final int MAX_PUBLIC_MEDIA = 500;
	private static final List<MediaStatus> VISIBLE_STATUSES = List.of(MediaStatus.STORED, MediaStatus.PROCESSING,
			MediaStatus.PROCESSED, MediaStatus.PROCESSING_FAILED);
	private static final List<MediaStatus> PUBLIC_VISIBLE_STATUSES = List.of(MediaStatus.STORED, MediaStatus.PROCESSING,
			MediaStatus.PROCESSED);
	private final MediaFileRepository mediaFiles;
	private final MediaThumbnailRepository thumbnails;
	private final GalleryRepository galleries;
	private final GalleryAccessService galleryAccess;
	private final EventService eventService;
	private final StorageService storage;

	@Transactional(readOnly = true)
	public List<MediaItemResponse> publicMedia(String slug, HttpSession session) {
		Gallery gallery = galleryAccess.requireGrant(slug, session, false).gallery();
		return mediaList(gallery.getId(), publicBase(slug), true, MAX_PUBLIC_MEDIA);
	}

	@Transactional(readOnly = true)
	public MediaResource publicResource(String slug, String mediaId, boolean thumbnail, HttpSession session) {
		Gallery gallery = galleryAccess.requireGrant(slug, session, false).gallery();
		return resource(gallery.getId(), mediaId, thumbnail, true);
	}

	@Transactional(readOnly = true)
	public List<MediaItemResponse> managedMedia(String eventId, String galleryId, String actorEmail) {
		requireAccessibleGallery(eventId, galleryId, actorEmail);
		return mediaList(galleryId, managedBase(eventId, galleryId), false, Integer.MAX_VALUE);
	}

	@Transactional(readOnly = true)
	public MediaResource managedResource(String eventId, String galleryId, String mediaId, boolean thumbnail,
			String actorEmail) {
		requireAccessibleGallery(eventId, galleryId, actorEmail);
		return resource(galleryId, mediaId, thumbnail, false);
	}

	@Transactional(readOnly = true)
	public GalleryDownload ownerDownload(String eventId, String galleryId, String actorEmail) {
		Gallery gallery = requireAccessibleGallery(eventId, galleryId, actorEmail);
		User actor = eventService.requireUser(actorEmail);
		Event event = eventService.requireAccessibleEvent(eventId, actor);
		if (eventService.roleFor(event, actor) != EventRole.OWNER)
			throw new AppException(EventErrorCode.EVENT_OWNER_REQUIRED);
		if (!gallery.isDownloadEnabled())
			throw new AppException(GalleryErrorCode.GALLERY_NOT_FOUND);
		List<MediaResource> files = mediaFiles
				.findByGalleryIdAndStatusInOrderByStoredAtDescCreatedAtDescIdAsc(galleryId, VISIBLE_STATUSES).stream()
				.filter(this::hasStoredObject).map(media -> originalResource(media, safeZipName(media))).toList();
		return new GalleryDownload(safeZipName(gallery.getName()) + ".zip", files);
	}

	public java.io.InputStream open(String objectKey) {
		return storage.open(objectKey);
	}

	private List<MediaItemResponse> mediaList(String galleryId, String basePath, boolean publicOnly, int limit) {
		List<MediaStatus> statuses = publicOnly ? PUBLIC_VISIBLE_STATUSES : VISIBLE_STATUSES;
		List<MediaFile> files = mediaFiles
				.findByGalleryIdAndStatusInOrderByStoredAtDescCreatedAtDescIdAsc(galleryId, statuses).stream()
				.filter(item -> (publicOnly ? PUBLIC_VISIBLE_STATUSES : VISIBLE_STATUSES).contains(item.getStatus()))
				.filter(item -> !publicOnly || item.getPublicationStatus() == null
						|| item.getPublicationStatus() == PublicationStatus.APPROVED)
				.limit(limit).filter(this::hasStoredObject).toList();
		if (files.isEmpty())
			return List.of();
		Map<String, MediaThumbnail> smallThumbnails = thumbnails
				.findByMediaFileIdInAndVariant(files.stream().map(MediaFile::getId).toList(),
						MediaThumbnailVariant.SMALL)
				.stream().collect(java.util.stream.Collectors.toMap(thumbnail -> thumbnail.getMediaFile().getId(),
						thumbnail -> thumbnail));
		return files.stream().map(media -> toResponse(media, smallThumbnails.get(media.getId()), basePath)).toList();
	}

	private MediaItemResponse toResponse(MediaFile media, MediaThumbnail thumbnail, String basePath) {
		String contentUrl = basePath + "/" + media.getId() + "/content";
		String thumbnailUrl = thumbnail != null ? basePath + "/" + media.getId() + "/thumbnail" : contentUrl;
		return new MediaItemResponse(media.getId(), media.getOriginalFilename(), media.getMediaType(),
				media.getStatus(), media.getSizeBytes() == null ? media.getExpectedSizeBytes() : media.getSizeBytes(),
				instant(media.getStoredAt()), thumbnailUrl, contentUrl, media.getPublicationStatus());
	}

	private MediaResource resource(String galleryId, String mediaId, boolean thumbnail, boolean publicOnly) {
		MediaFile media = mediaFiles.findByIdAndGalleryId(mediaId, galleryId)
				.filter(item -> (publicOnly ? PUBLIC_VISIBLE_STATUSES : VISIBLE_STATUSES).contains(item.getStatus()))
				.filter(item -> !publicOnly || item.getPublicationStatus() == null
						|| item.getPublicationStatus() == PublicationStatus.APPROVED)
				.filter(this::hasStoredObject)
				.orElseThrow(() -> new AppException(GalleryErrorCode.PUBLIC_GALLERY_NOT_FOUND));
		if (thumbnail && media.getMediaType() == MediaType.IMAGE) {
			return thumbnails.findByMediaFileIdAndVariant(mediaId, MediaThumbnailVariant.SMALL)
					.filter(item -> storage.exists(item.getStorageKey()))
					.map(item -> new MediaResource(item.getStorageKey(), media.getOriginalFilename(), "image/jpeg",
							item.getSizeBytes()))
					.orElseGet(() -> originalResource(media, media.getOriginalFilename()));
		}
		return originalResource(media, media.getOriginalFilename());
	}

	private MediaResource originalResource(MediaFile media, String fileName) {
		return new MediaResource(media.getStorageKey(), fileName, contentType(media),
				media.getSizeBytes() == null ? media.getExpectedSizeBytes() : media.getSizeBytes());
	}

	private Gallery requireAccessibleGallery(String eventId, String galleryId, String actorEmail) {
		User actor = eventService.requireUser(actorEmail);
		eventService.requireAccessibleEvent(eventId, actor);
		return galleries.findByIdAndEventIdAndDeletedAtIsNull(galleryId, eventId)
				.orElseThrow(() -> new AppException(GalleryErrorCode.GALLERY_NOT_FOUND));
	}

	private boolean hasStoredObject(MediaFile media) {
		return visibleStatus(media) && media.getStoredAt() != null && storage.exists(media.getStorageKey());
	}

	private boolean visibleStatus(MediaFile media) {
		return VISIBLE_STATUSES.contains(media.getStatus());
	}

	private String contentType(MediaFile media) {
		if (media.getDetectedContentType() != null && !media.getDetectedContentType().isBlank())
			return media.getDetectedContentType();
		if (media.getDeclaredContentType() != null && !media.getDeclaredContentType().isBlank())
			return media.getDeclaredContentType();
		return "application/octet-stream";
	}

	private String publicBase(String slug) {
		return "/api/public/galleries/" + slug + "/media";
	}

	private String managedBase(String eventId, String galleryId) {
		return "/api/events/" + eventId + "/galleries/" + galleryId + "/media";
	}

	private Instant instant(java.time.LocalDateTime value) {
		return value == null ? null : value.toInstant(ZoneOffset.UTC);
	}

	private String safeZipName(MediaFile media) {
		String name = safeZipName(media.getOriginalFilename());
		int dot = name.lastIndexOf('.');
		String base = dot > 0 ? name.substring(0, dot) : name;
		String extension = dot > 0 ? name.substring(dot) : "";
		return base + "-" + media.getId().substring(0, 8) + extension;
	}

	private String safeZipName(String name) {
		String normalized = name == null
				? "gallery"
				: name.trim().replace('\\', '-').replace('/', '-').replaceAll("[\\x00-\\x1F]+", "").replaceAll("\\s+",
						" ");
		normalized = normalized.replaceAll("[^A-Za-z0-9._ -]", "-").replaceAll("-+", "-").trim();
		if (normalized.isBlank() || normalized.equals(".") || normalized.equals(".."))
			return "gallery";
		return normalized.substring(0, Math.min(normalized.length(), 120));
	}
}
