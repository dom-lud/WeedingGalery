package pl.backend.weddinggallery.gallery.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.service.EventService;
import pl.backend.weddinggallery.gallery.dto.*;
import pl.backend.weddinggallery.gallery.exception.GalleryErrorCode;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.membership.model.EventRole;
import pl.backend.weddinggallery.media.model.MediaStatus;
import pl.backend.weddinggallery.media.repository.MediaFileRepository;
@Service
@RequiredArgsConstructor
public class GalleryCustomizationService {
	private final GalleryRepository galleries;
	private final EventService events;
	private final AuditService audit;
	private final MediaFileRepository mediaFiles;
	@Transactional(readOnly = true)
	public GalleryCustomizationResponse get(String id, String email) {
		Gallery g = require(id);
		access(g, email, false);
		return out(g);
	}
	@Transactional
	public GalleryCustomizationResponse update(String id, GalleryCustomizationRequest r, String email) {
		Gallery g = require(id);
		access(g, email, true);
		if (r.version() != g.getVersion())
			throw new AppException(GalleryErrorCode.GALLERY_CUSTOMIZATION_CONFLICT);
		g.setTheme(r.theme());
		g.setLayout(r.layout());
		g.setPrimaryColor(r.primaryColor().toUpperCase());
		g.setAccentColor(r.accentColor().toUpperCase());
		g.setBackgroundColor(r.backgroundColor().toUpperCase());
		g.setWelcomeText(r.welcomeText() == null ? "" : r.welcomeText());
		g.setShowTitle(r.showTitle());
		g.setShowUpload(r.showUpload());
		g.setShowDownload(r.showDownload());
		if (r.coverMediaId() != null) {
			mediaFiles.findByIdAndGalleryId(r.coverMediaId(), g.getId())
					.filter(media -> media.getStatus() == MediaStatus.PROCESSED)
					.orElseThrow(() -> new AppException(GalleryErrorCode.GALLERY_CUSTOMIZATION_COVER_INVALID));
		}
		g.setCoverMediaId(r.coverMediaId());
		g.setUpdatedAt(java.time.LocalDateTime.now());
		galleries.save(g);
		galleries.flush();
		return out(g);
	}
	private Gallery require(String id) {
		return galleries.findById(id).filter(g -> g.getDeletedAt() == null)
				.orElseThrow(() -> new AppException(GalleryErrorCode.GALLERY_NOT_FOUND));
	}
	private void access(Gallery g, String e, boolean owner) {
		var u = events.requireUser(e);
		var ev = events.requireAccessibleEvent(g.getEvent().getId(), u);
		if (owner && events.roleFor(ev, u) != EventRole.OWNER)
			throw new AppException(GalleryErrorCode.GALLERY_SETTINGS_OWNER_REQUIRED);
	}
	private GalleryCustomizationResponse out(Gallery g) {
		return new GalleryCustomizationResponse(g.getTheme(), g.getLayout(), g.getPrimaryColor(), g.getAccentColor(),
				g.getBackgroundColor(), g.getWelcomeText(), g.isShowTitle(), g.isShowUpload(), g.isShowDownload(),
				g.getCoverMediaId(), g.getVersion());
	}
}
