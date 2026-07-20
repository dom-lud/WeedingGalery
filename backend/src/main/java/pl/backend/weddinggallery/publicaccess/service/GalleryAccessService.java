package pl.backend.weddinggallery.publicaccess.service;

import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.model.EventStatus;
import pl.backend.weddinggallery.event.service.EventService;
import pl.backend.weddinggallery.gallery.exception.GalleryErrorCode;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.model.GalleryStatus;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.membership.model.EventRole;
import pl.backend.weddinggallery.publicaccess.dto.*;
import pl.backend.weddinggallery.publicaccess.model.GalleryAccess;
import pl.backend.weddinggallery.publicaccess.repository.GalleryAccessRepository;
import pl.backend.weddinggallery.user.model.User;

@Service
@RequiredArgsConstructor
public class GalleryAccessService {
	private static final String GRANT_PREFIX = "PUBLIC_GALLERY_GRANT:";
	private final GalleryRepository galleryRepository;
	private final GalleryAccessRepository accessRepository;
	private final EventService eventService;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;
	private final AuditService auditService;

	@Transactional(readOnly = true)
	public GallerySettingsResponse getSettings(String eventId, String galleryId, String email) {
		AccessContext context = accessible(eventId, galleryId, email);
		return settings(context.gallery());
	}

	@Transactional
	public GallerySettingsResponse updateSettings(String eventId, String galleryId, GallerySettingsRequest request,
			String email) {
		AccessContext context = owner(eventId, galleryId, email);
		Gallery gallery = context.gallery();
		requireMutable(gallery);
		if (gallery.getVersion() != request.version()) {
			throw new org.springframework.orm.ObjectOptimisticLockingFailureException(Gallery.class, galleryId);
		}
		if (request.publicViewEnabled() && accessRepository.findByGalleryIdAndRevokedAtIsNull(galleryId).isEmpty()) {
			throw new AppException(GalleryErrorCode.GALLERY_ACCESS_TOKEN_REQUIRED);
		}
		if (request.uploadEnabled() && !request.publicViewEnabled()) {
			throw new AppException(GalleryErrorCode.GALLERY_INVALID_PUBLICATION_WINDOW,
					"Upload requires public view to be enabled.");
		}
		LocalDateTime publishedAt = utc(request.publishedAt());
		LocalDateTime expiresAt = utc(request.expiresAt());
		if (publishedAt != null && expiresAt != null && !expiresAt.isAfter(publishedAt)) {
			throw new AppException(GalleryErrorCode.GALLERY_INVALID_PUBLICATION_WINDOW);
		}
		gallery.setPublicViewEnabled(request.publicViewEnabled());
		gallery.setUploadEnabled(request.uploadEnabled());
		gallery.setDownloadEnabled(request.downloadEnabled());
		gallery.setModerationMode(request.moderationMode());
		gallery.setPublishedAt(publishedAt);
		gallery.setExpiresAt(expiresAt);
		gallery.setUpdatedAt(LocalDateTime.now());
		auditService.logRequiredGalleryEvent(context.actor().getEmail(), EventType.GALLERY_SETTINGS_UPDATED, eventId,
				galleryId, "publicView=" + request.publicViewEnabled() + ",upload=" + request.uploadEnabled());
		galleryRepository.flush();
		return settings(gallery);
	}

	@Transactional
	public AccessTokenResponse rotateToken(String eventId, String galleryId, String email) {
		AccessContext context = owner(eventId, galleryId, email);
		requireMutable(context.gallery());
		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		List<GalleryAccess> active = accessRepository.findByGalleryIdAndRevokedAtIsNull(galleryId);
		active.forEach(access -> access.setRevokedAt(now));
		String raw = tokenService.generate();
		accessRepository
				.save(GalleryAccess.builder().gallery(context.gallery()).tokenHash(tokenService.hash(raw)).build());
		auditService.logRequiredGalleryEvent(context.actor().getEmail(), EventType.GALLERY_ACCESS_TOKEN_ROTATED,
				eventId, galleryId, "token=rotated");
		return new AccessTokenResponse(raw, "/g/" + context.gallery().getSlug() + "#token=" + raw);
	}

	@Transactional
	public void setAccessCode(String eventId, String galleryId, String code, String email) {
		AccessContext context = owner(eventId, galleryId, email);
		requireMutable(context.gallery());
		context.gallery().setAccessCodeHash(passwordEncoder.encode(code));
		context.gallery().setUpdatedAt(LocalDateTime.now());
		auditService.logRequiredGalleryEvent(context.actor().getEmail(), EventType.GALLERY_ACCESS_CODE_CHANGED, eventId,
				galleryId, "accessCode=set");
	}

	@Transactional
	public void clearAccessCode(String eventId, String galleryId, String email) {
		AccessContext context = owner(eventId, galleryId, email);
		requireMutable(context.gallery());
		context.gallery().setAccessCodeHash(null);
		context.gallery().setUpdatedAt(LocalDateTime.now());
		auditService.logRequiredGalleryEvent(context.actor().getEmail(), EventType.GALLERY_ACCESS_CODE_CHANGED, eventId,
				galleryId, "accessCode=cleared");
	}

	@Transactional(readOnly = true)
	public PublicGalleryResponse exchange(String slug, PublicAccessRequest request, HttpSession session) {
		Gallery gallery = availableGallery(slug);
		GalleryAccess access = accessRepository
				.findByGalleryIdAndTokenHashAndRevokedAtIsNull(gallery.getId(),
						tokenService.hash(request.accessToken()))
				.orElseThrow(() -> new AppException(GalleryErrorCode.PUBLIC_GALLERY_NOT_FOUND));
		if (gallery.getAccessCodeHash() != null && (request.accessCode() == null || request.accessCode().isBlank())) {
			throw new AppException(GalleryErrorCode.GALLERY_ACCESS_CODE_REQUIRED);
		}
		if (gallery.getAccessCodeHash() != null
				&& !passwordEncoder.matches(request.accessCode(), gallery.getAccessCodeHash())) {
			throw new AppException(GalleryErrorCode.GALLERY_ACCESS_DENIED);
		}
		session.setAttribute(GRANT_PREFIX + slug,
				new PublicGrant(access.getId(), gallery.getId(), Instant.now().plus(30, ChronoUnit.MINUTES)));
		return publicResponse(gallery);
	}

	@Transactional(readOnly = true)
	public PublicGalleryResponse getPublic(String slug, HttpSession session) {
		requireGrant(slug, session, false);
		return publicResponse(availableGallery(slug));
	}

	@Transactional(readOnly = true)
	public GrantedGallery requireGrant(String slug, HttpSession session, boolean uploadRequired) {
		Object raw = session.getAttribute(GRANT_PREFIX + slug);
		if (!(raw instanceof PublicGrant grant) || grant.expiresAt().isBefore(Instant.now())) {
			throw new AppException(GalleryErrorCode.PUBLIC_GALLERY_NOT_FOUND);
		}
		Gallery gallery = availableGallery(slug);
		if (!gallery.getId().equals(grant.galleryId()) || accessRepository.findById(grant.accessId())
				.filter(access -> access.getRevokedAt() == null && access.getGallery().getId().equals(gallery.getId()))
				.isEmpty()) {
			throw new AppException(GalleryErrorCode.PUBLIC_GALLERY_NOT_FOUND);
		}
		if (uploadRequired && !gallery.isUploadEnabled()) {
			throw new AppException(GalleryErrorCode.PUBLIC_GALLERY_NOT_FOUND);
		}
		return new GrantedGallery(gallery, grant.accessId(), session.getId());
	}

	private Gallery availableGallery(String slug) {
		Gallery gallery = galleryRepository.findBySlugAndDeletedAtIsNull(slug)
				.orElseThrow(() -> new AppException(GalleryErrorCode.PUBLIC_GALLERY_NOT_FOUND));
		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		boolean unavailable = gallery.getStatus() != GalleryStatus.ACTIVE || !gallery.isPublicViewEnabled()
				|| gallery.getEvent().getStatus() == EventStatus.ARCHIVED
				|| gallery.getEvent().getStatus() == EventStatus.DELETED
				|| (gallery.getPublishedAt() != null && now.isBefore(gallery.getPublishedAt()))
				|| (gallery.getExpiresAt() != null && !now.isBefore(gallery.getExpiresAt()));
		if (unavailable)
			throw new AppException(GalleryErrorCode.PUBLIC_GALLERY_NOT_FOUND);
		return gallery;
	}

	private AccessContext accessible(String eventId, String galleryId, String email) {
		User actor = eventService.requireUser(email);
		var event = eventService.requireAccessibleEvent(eventId, actor);
		EventRole role = eventService.roleFor(event, actor);
		Gallery gallery = galleryRepository.findByIdAndEventIdAndDeletedAtIsNull(galleryId, eventId)
				.orElseThrow(() -> new AppException(GalleryErrorCode.GALLERY_NOT_FOUND));
		return new AccessContext(actor, gallery, role);
	}

	private AccessContext owner(String eventId, String galleryId, String email) {
		AccessContext context = accessible(eventId, galleryId, email);
		if (context.role() != EventRole.OWNER)
			throw new AppException(GalleryErrorCode.GALLERY_SETTINGS_OWNER_REQUIRED);
		return context;
	}

	private void requireMutable(Gallery gallery) {
		if (gallery.getStatus() != GalleryStatus.ACTIVE || gallery.getEvent().getStatus() == EventStatus.ARCHIVED
				|| gallery.getEvent().getStatus() == EventStatus.DELETED) {
			throw new AppException(GalleryErrorCode.GALLERY_ARCHIVED);
		}
	}

	private GallerySettingsResponse settings(Gallery gallery) {
		return new GallerySettingsResponse(gallery.isPublicViewEnabled(), gallery.isUploadEnabled(),
				gallery.isDownloadEnabled(), gallery.getModerationMode(),
				!accessRepository.findByGalleryIdAndRevokedAtIsNull(gallery.getId()).isEmpty(),
				gallery.getAccessCodeHash() != null, instant(gallery.getPublishedAt()), instant(gallery.getExpiresAt()),
				gallery.getVersion());
	}

	private PublicGalleryResponse publicResponse(Gallery gallery) {
		return new PublicGalleryResponse(gallery.getSlug(), gallery.getName(), gallery.getDescription(),
				gallery.isUploadEnabled(), gallery.isDownloadEnabled(), gallery.getModerationMode(),
				instant(gallery.getPublishedAt()), instant(gallery.getExpiresAt()));
	}

	private LocalDateTime utc(Instant instant) {
		return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
	}
	private Instant instant(LocalDateTime value) {
		return value == null ? null : value.toInstant(ZoneOffset.UTC);
	}
	private record AccessContext(User actor, Gallery gallery, EventRole role) {
	}
	public record GrantedGallery(Gallery gallery, String accessId, String httpSessionId) {
	}
	private record PublicGrant(String accessId, String galleryId, Instant expiresAt) implements Serializable {
	}
}
