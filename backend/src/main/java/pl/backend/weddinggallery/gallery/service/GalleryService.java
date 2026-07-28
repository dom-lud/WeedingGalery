package pl.backend.weddinggallery.gallery.service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.exception.EventErrorCode;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.event.model.EventStatus;
import pl.backend.weddinggallery.event.service.EventService;
import pl.backend.weddinggallery.gallery.dto.GalleryResponse;
import pl.backend.weddinggallery.gallery.dto.GalleryWriteRequest;
import pl.backend.weddinggallery.gallery.exception.GalleryErrorCode;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.model.GalleryStatus;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.membership.model.EventRole;
import pl.backend.weddinggallery.user.model.User;

@Service
@RequiredArgsConstructor
public class GalleryService {
	private static final int SLUG_ATTEMPTS = 5;
	private final GalleryRepository galleryRepository;
	private final EventService eventService;
	private final AuditService auditService;

	@Transactional(readOnly = true)
	public List<GalleryResponse> list(String eventId, String actorEmail) {
		User actor = eventService.requireUser(actorEmail);
		Event event = eventService.requireAccessibleEvent(eventId, actor);
		EventRole role = eventService.roleFor(event, actor);
		return galleryRepository.findByEventIdAndDeletedAtIsNullOrderBySortOrderAscCreatedAtAscIdAsc(eventId).stream()
				.map(gallery -> toResponse(gallery, role)).toList();
	}

	@Transactional(readOnly = true)
	public GalleryResponse get(String eventId, String galleryId, String actorEmail) {
		Access access = requireAccessible(eventId, actorEmail);
		return toResponse(requireGallery(eventId, galleryId), access.role());
	}

	@Transactional(readOnly = true)
	public String publicSlug(String eventId, String galleryId, String actorEmail) {
		requireAccessible(eventId, actorEmail);
		return requireGallery(eventId, galleryId).getSlug();
	}

	@Transactional
	public GalleryResponse create(String eventId, GalleryWriteRequest request, String actorEmail) {
		Access access = requireAccessible(eventId, actorEmail);
		requireActiveEvent(access.event());
		Gallery gallery = Gallery.builder().event(access.event()).name(request.name().trim())
				.description(normalizeDescription(request.description())).sortOrder(request.sortOrder())
				.slug(generateSlug(request.name())).status(GalleryStatus.ACTIVE).build();
		galleryRepository.save(gallery);
		auditService.logRequiredGalleryEvent(access.actor().getEmail(), EventType.GALLERY_CREATED, eventId,
				gallery.getId(), "status=ACTIVE");
		return toResponse(gallery, access.role());
	}

	@Transactional
	public GalleryResponse update(String eventId, String galleryId, GalleryWriteRequest request, String actorEmail) {
		Access access = requireAccessible(eventId, actorEmail);
		requireActiveEvent(access.event());
		Gallery gallery = requireGallery(eventId, galleryId);
		if (gallery.getStatus() == GalleryStatus.ARCHIVED) {
			throw new AppException(GalleryErrorCode.GALLERY_ARCHIVED);
		}
		gallery.setName(request.name().trim());
		gallery.setDescription(normalizeDescription(request.description()));
		gallery.setSortOrder(request.sortOrder());
		gallery.setUpdatedAt(LocalDateTime.now());
		auditService.logRequiredGalleryEvent(access.actor().getEmail(), EventType.GALLERY_UPDATED, eventId, galleryId,
				"metadata=updated");
		return toResponse(gallery, access.role());
	}

	@Transactional
	public GalleryResponse archive(String eventId, String galleryId, String actorEmail) {
		Access access = requireOwner(eventId, actorEmail);
		Gallery gallery = requireGallery(eventId, galleryId);
		if (gallery.getStatus() != GalleryStatus.ARCHIVED) {
			gallery.setStatus(GalleryStatus.ARCHIVED);
			gallery.setArchivedAt(LocalDateTime.now());
			gallery.setUpdatedAt(LocalDateTime.now());
			auditService.logRequiredGalleryEvent(access.actor().getEmail(), EventType.GALLERY_ARCHIVED, eventId,
					galleryId, "status=ARCHIVED");
		}
		return toResponse(gallery, EventRole.OWNER);
	}

	@Transactional
	public void delete(String eventId, String galleryId, String actorEmail) {
		Access access = requireOwner(eventId, actorEmail);
		Gallery gallery = requireGallery(eventId, galleryId);
		gallery.setStatus(GalleryStatus.DELETED);
		gallery.setDeletedAt(LocalDateTime.now());
		gallery.setUpdatedAt(LocalDateTime.now());
		auditService.logRequiredGalleryEvent(access.actor().getEmail(), EventType.GALLERY_DELETED, eventId, galleryId,
				"status=DELETED");
	}

	private Access requireAccessible(String eventId, String actorEmail) {
		User actor = eventService.requireUser(actorEmail);
		Event event = eventService.requireAccessibleEvent(eventId, actor);
		return new Access(actor, event, eventService.roleFor(event, actor));
	}

	private Access requireOwner(String eventId, String actorEmail) {
		Access access = requireAccessible(eventId, actorEmail);
		if (access.role() != EventRole.OWNER) {
			throw new AppException(GalleryErrorCode.GALLERY_OWNER_REQUIRED);
		}
		return access;
	}

	private Gallery requireGallery(String eventId, String galleryId) {
		return galleryRepository.findByIdAndEventIdAndDeletedAtIsNull(galleryId, eventId)
				.orElseThrow(() -> new AppException(GalleryErrorCode.GALLERY_NOT_FOUND));
	}

	private void requireActiveEvent(Event event) {
		if (event.getStatus() == EventStatus.ARCHIVED) {
			throw new AppException(EventErrorCode.EVENT_ARCHIVED);
		}
	}

	private String generateSlug(String name) {
		String base = Normalizer.normalize(name.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "").replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+|-+$)", "");
		if (base.isBlank()) {
			base = "gallery";
		}
		base = base.substring(0, Math.min(base.length(), 220));
		for (int attempt = 0; attempt < SLUG_ATTEMPTS; attempt++) {
			String slug = base + "-" + UUID.randomUUID().toString().substring(0, 8);
			if (!galleryRepository.existsBySlug(slug)) {
				return slug;
			}
		}
		throw new AppException(GalleryErrorCode.GALLERY_SLUG_CONFLICT);
	}

	private GalleryResponse toResponse(Gallery gallery, EventRole role) {
		return new GalleryResponse(gallery.getId(), gallery.getEvent().getId(), gallery.getName(), gallery.getSlug(),
				gallery.getDescription(), gallery.getSortOrder(), gallery.getStatus(), role,
				gallery.getCreatedAt().toInstant(ZoneOffset.UTC), gallery.getUpdatedAt().toInstant(ZoneOffset.UTC));
	}

	private String normalizeDescription(String description) {
		if (description == null) {
			return null;
		}
		String trimmed = description.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private record Access(User actor, Event event, EventRole role) {
	}
}
