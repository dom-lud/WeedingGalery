package pl.backend.weddinggallery.media.dto;

import pl.backend.weddinggallery.media.model.PublicationStatus;

public record MediaModerationResponse(String mediaId, PublicationStatus publicationStatus, boolean changed) {
}
