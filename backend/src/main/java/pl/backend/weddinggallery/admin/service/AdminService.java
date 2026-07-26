package pl.backend.weddinggallery.admin.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.backend.weddinggallery.admin.dto.*;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.model.AuditEvent;
import pl.backend.weddinggallery.audit.repository.AuditEventRepository;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.event.model.EventStatus;
import pl.backend.weddinggallery.event.repository.EventRepository;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.media.model.MediaFile;
import pl.backend.weddinggallery.media.model.PublicationStatus;
import pl.backend.weddinggallery.media.repository.MediaFileRepository;
import pl.backend.weddinggallery.user.model.SystemRole;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AdminService {
	private static final int MAX_PAGE_SIZE = 100;
	private final UserRepository userRepository;
	private final EventRepository eventRepository;
	private final GalleryRepository galleryRepository;
	private final MediaFileRepository mediaFileRepository;
	private final AuditEventRepository auditEventRepository;
	private final AuditService auditService;

	@Transactional(readOnly = true)
	public boolean isAdmin(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			return false;
		}
		return userRepository.findByEmail(authentication.getName().toLowerCase())
				.map(user -> user.getSystemRole() == SystemRole.ADMIN).orElse(false);
	}

	@Transactional(readOnly = true)
	public AdminDashboardResponse dashboard() {
		List<Gallery> galleries = galleryRepository.findAll();
		long storage = galleries.stream().mapToLong(Gallery::getStorageUsedBytes).sum();
		long locked = userRepository.findAll().stream().filter(this::isLocked).count();
		return new AdminDashboardResponse(userRepository.count(), locked, eventRepository.count(),
				galleryRepository.count(), mediaFileRepository.count(), storage, auditEventRepository.count());
	}

	@Transactional(readOnly = true)
	public AdminPageResponse<AdminUserResponse> users(Pageable pageable) {
		return page(userRepository.findAll(pageable).map(this::toUser));
	}

	@Transactional(readOnly = true)
	public AdminPageResponse<AdminEventResponse> events(Pageable pageable) {
		return page(eventRepository.findAll(pageable).map(this::toEvent));
	}

	@Transactional(readOnly = true)
	public AdminPageResponse<AdminGalleryResponse> galleries(Pageable pageable) {
		return page(galleryRepository.findAll(pageable).map(this::toGallery));
	}

	@Transactional(readOnly = true)
	public AdminPageResponse<AdminMediaResponse> media(Pageable pageable) {
		return page(mediaFileRepository.findAll(pageable).map(this::toMedia));
	}

	@Transactional(readOnly = true)
	public AdminPageResponse<AdminAuditResponse> audit(Pageable pageable) {
		return page(auditEventRepository.findAll(pageable).map(this::toAudit));
	}

	@Transactional
	public AdminUserResponse lockUser(String actorEmail, String userId) {
		User target = userRepository.findById(userId).orElseThrow(() -> new AdminNotFoundException("User not found"));
		if (!isLocked(target)) {
			target.setLockedUntil(LocalDateTime.now().plusMinutes(15));
			userRepository.save(target);
			auditService.logRequiredEvent(actorEmail, EventType.USER_LOGIN_BLOCKED, null, target.getId(),
					"ADMIN_ACTION=LOCK_USER");
		}
		return toUser(target);
	}

	@Transactional
	public AdminUserResponse unlockUser(String actorEmail, String userId) {
		User target = userRepository.findById(userId).orElseThrow(() -> new AdminNotFoundException("User not found"));
		if (target.getLockedUntil() != null || target.getFailedLoginAttempts() != 0) {
			target.setLockedUntil(null);
			target.setFailedLoginAttempts(0);
			userRepository.save(target);
			auditService.logRequiredEvent(actorEmail, EventType.USER_LOGIN_BLOCKED, null, target.getId(),
					"ADMIN_ACTION=UNLOCK_USER");
		}
		return toUser(target);
	}

	@Transactional
	public AdminEventResponse archiveEvent(String actorEmail, String eventId) {
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new AdminNotFoundException("Event not found"));
		if (event.getDeletedAt() == null && event.getStatus() != EventStatus.ARCHIVED) {
			event.setStatus(EventStatus.ARCHIVED);
			event.setArchivedAt(LocalDateTime.now());
			eventRepository.save(event);
			auditService.logRequiredEvent(actorEmail, EventType.EVENT_ARCHIVED, event.getId(), null,
					"ADMIN_ACTION=ARCHIVE_EVENT");
		}
		return toEvent(event);
	}

	@Transactional
	public AdminMediaResponse hideMedia(String actorEmail, String mediaId) {
		MediaFile media = mediaFileRepository.findById(mediaId)
				.orElseThrow(() -> new AdminNotFoundException("Media not found"));
		if (media.getPublicationStatus() != PublicationStatus.HIDDEN) {
			media.setPublicationStatus(PublicationStatus.HIDDEN);
			mediaFileRepository.save(media);
			Gallery gallery = media.getGallery();
			auditService.logRequiredGalleryEvent(actorEmail, EventType.MEDIA_PUBLICATION_HIDDEN,
					gallery.getEvent().getId(), gallery.getId(), "ADMIN_ACTION=HIDE_MEDIA mediaId=" + media.getId());
		}
		return toMedia(media);
	}

	public static int maxPageSize() {
		return MAX_PAGE_SIZE;
	}

	private boolean isLocked(User user) {
		return user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now());
	}

	private AdminUserResponse toUser(User user) {
		return new AdminUserResponse(user.getId(), user.getEmail(), user.getSystemRole(), isLocked(user),
				user.getLockedUntil(), user.getCreatedAt());
	}

	private AdminEventResponse toEvent(Event event) {
		return new AdminEventResponse(event.getId(), event.getName(), event.getType(), event.getStatus(),
				event.getOwner().getId(), event.getOwner().getEmail(), event.getEventDate(), event.getCreatedAt(),
				event.getUpdatedAt());
	}

	private AdminGalleryResponse toGallery(Gallery gallery) {
		return new AdminGalleryResponse(gallery.getId(), gallery.getEvent().getId(), gallery.getName(),
				gallery.getSlug(), gallery.getStatus(), gallery.isPublicViewEnabled(), gallery.getStorageUsedBytes(),
				gallery.getStorageReservedBytes(), gallery.getCreatedAt());
	}

	private AdminMediaResponse toMedia(MediaFile media) {
		return new AdminMediaResponse(media.getId(), media.getGallery().getId(), media.getGallery().getEvent().getId(),
				media.getOriginalFilename(), media.getMediaType(), media.getStatus(), media.getPublicationStatus(),
				media.getSizeBytes(), media.getStoredAt());
	}

	private AdminAuditResponse toAudit(AuditEvent event) {
		String resourceType = event.getGalleryId() != null
				? "GALLERY"
				: event.getEventId() != null ? "EVENT" : event.getTargetUserId() != null ? "USER" : null;
		String resourceId = event.getGalleryId() != null
				? event.getGalleryId()
				: event.getEventId() != null ? event.getEventId() : event.getTargetUserId();
		return new AdminAuditResponse(event.getId(), event.getEventType(), event.getUserEmail(), resourceType,
				resourceId, event.getCreatedAt(), event.getDetails());
	}

	private <T> AdminPageResponse<T> page(Page<T> page) {
		return new AdminPageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
				page.getTotalPages());
	}

	public static class AdminNotFoundException extends RuntimeException {
		public AdminNotFoundException(String message) {
			super(message);
		}
	}
}
