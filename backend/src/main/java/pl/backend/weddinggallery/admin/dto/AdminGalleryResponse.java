package pl.backend.weddinggallery.admin.dto;

import java.time.LocalDateTime;
import pl.backend.weddinggallery.gallery.model.GalleryStatus;

public record AdminGalleryResponse(String id, String eventId, String name, String slug, GalleryStatus status,
		boolean publicViewEnabled, long storageUsedBytes, long storageReservedBytes, LocalDateTime createdAt) {
}
