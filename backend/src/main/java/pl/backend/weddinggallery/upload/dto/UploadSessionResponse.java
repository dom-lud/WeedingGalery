package pl.backend.weddinggallery.upload.dto;

import java.time.Instant;
import java.util.List;
import pl.backend.weddinggallery.upload.model.UploadSessionStatus;

public record UploadSessionResponse(String id, UploadSessionStatus status, Instant expiresAt,
		List<UploadFileResponse> files) {
}
