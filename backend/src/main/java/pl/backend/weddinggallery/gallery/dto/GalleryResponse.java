package pl.backend.weddinggallery.gallery.dto;

import java.time.Instant;
import pl.backend.weddinggallery.gallery.model.GalleryStatus;
import pl.backend.weddinggallery.membership.model.EventRole;

public record GalleryResponse(String id, String eventId, String name, String slug, String description, int sortOrder,
		GalleryStatus status, EventRole currentUserRole, Instant createdAt, Instant updatedAt) {
}
