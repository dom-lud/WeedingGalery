package pl.backend.weddinggallery.statistics.service;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.event.model.Event;
import pl.backend.weddinggallery.event.service.EventService;
import pl.backend.weddinggallery.gallery.exception.GalleryErrorCode;
import pl.backend.weddinggallery.gallery.model.Gallery;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.statistics.dto.StatisticsResponse;
import pl.backend.weddinggallery.statistics.model.UsageEvent;
import pl.backend.weddinggallery.statistics.model.UsageEventType;
import pl.backend.weddinggallery.statistics.repository.UsageEventRepository;

@Service
@RequiredArgsConstructor
public class StatisticsService {
	private final EntityManager entityManager;
	private final EventService eventService;
	private final GalleryRepository galleryRepository;
	private final UsageEventRepository usageEventRepository;

	@Transactional(readOnly = true)
	public StatisticsResponse forEvent(String eventId, String actorEmail) {
		Event event = eventService.requireAccessibleEvent(eventId, eventService.requireUser(actorEmail));
		return aggregate(event.getId(), null);
	}

	@Transactional(readOnly = true)
	public StatisticsResponse forGallery(String eventId, String galleryId, String actorEmail) {
		Event event = eventService.requireAccessibleEvent(eventId, eventService.requireUser(actorEmail));
		Gallery gallery = galleryRepository.findByIdAndEventIdAndDeletedAtIsNull(galleryId, event.getId())
				.orElseThrow(() -> new AppException(GalleryErrorCode.GALLERY_NOT_FOUND));
		return aggregate(event.getId(), gallery.getId());
	}

	@Transactional(readOnly = true)
	public StatisticsResponse global() {
		return aggregate(null, null);
	}

	private StatisticsResponse aggregate(String eventId, String galleryId) {
		String mediaScope = galleryId != null
				? "m.gallery.id = :galleryId"
				: eventId != null ? "m.gallery.event.id = :eventId" : "1=1";
		String uploadScope = galleryId != null
				? "u.gallery.id = :galleryId"
				: eventId != null ? "u.gallery.event.id = :eventId" : "1=1";
		String galleryScope = galleryId != null
				? "g.id = :galleryId"
				: eventId != null ? "g.event.id = :eventId" : "1=1";
		long mediaCount = count("select count(m) from MediaFile m where " + mediaScope, eventId, galleryId);
		long imageCount = count("select count(m) from MediaFile m where m.mediaType = 'IMAGE' and " + mediaScope,
				eventId, galleryId);
		long videoCount = count("select count(m) from MediaFile m where m.mediaType = 'VIDEO' and " + mediaScope,
				eventId, galleryId);
		long uploadSessions = count("select count(u) from UploadSession u where " + uploadScope, eventId, galleryId);
		long storage = number("select coalesce(sum(g.storageUsedBytes), 0) from Gallery g where " + galleryScope,
				eventId, galleryId);
		long processingFailures = count(
				"select count(m) from MediaFile m where m.status = 'PROCESSING_FAILED' and " + mediaScope, eventId,
				galleryId);
		long publicViews = usageCount(UsageEventType.GALLERY_VIEW, eventId, galleryId);
		long downloads = usageCount(UsageEventType.MEDIA_DOWNLOAD, eventId, galleryId);
		long usageEvents = usageCount(null, eventId, galleryId);
		return new StatisticsResponse(eventId, galleryId, mediaCount, imageCount, videoCount, uploadSessions,
				mediaCount, storage, processingFailures, publicViews, downloads, usageEvents);
	}

	private long count(String jpql, String eventId, String galleryId) {
		var query = entityManager.createQuery(jpql, Long.class);
		bind(query, eventId, galleryId);
		return query.getSingleResult();
	}

	private long number(String jpql, String eventId, String galleryId) {
		var query = entityManager.createQuery(jpql, Long.class);
		bind(query, eventId, galleryId);
		return query.getSingleResult();
	}

	private void bind(jakarta.persistence.TypedQuery<Long> query, String eventId, String galleryId) {
		if (eventId != null)
			query.setParameter("eventId", eventId);
		if (galleryId != null)
			query.setParameter("galleryId", galleryId);
	}

	private long usageCount(UsageEventType type, String eventId, String galleryId) {
		String scope = galleryId != null
				? "u.galleryId = :galleryId"
				: eventId != null ? "u.eventId = :eventId" : "1=1";
		String typeScope = type == null ? "1=1" : "u.eventType = :type";
		var query = entityManager.createQuery(
				"select coalesce(sum(u.quantity), 0) from UsageEvent u where " + scope + " and " + typeScope,
				Long.class);
		if (eventId != null)
			query.setParameter("eventId", eventId);
		if (galleryId != null)
			query.setParameter("galleryId", galleryId);
		if (type != null)
			query.setParameter("type", type);
		return query.getSingleResult();
	}

	@Transactional
	public void record(UsageEventType type, String eventId, String galleryId, String actorUserId, String publicAccessId,
			long quantity) {
		if (type == null || quantity < 1)
			throw new IllegalArgumentException("Usage event type and positive quantity are required");
		usageEventRepository.save(UsageEvent.builder().eventType(type).eventId(eventId).galleryId(galleryId)
				.actorUserId(actorUserId).publicAccessId(publicAccessId).quantity(quantity).build());
	}
}
