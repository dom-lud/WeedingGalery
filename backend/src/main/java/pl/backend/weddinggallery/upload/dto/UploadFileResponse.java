package pl.backend.weddinggallery.upload.dto;

import pl.backend.weddinggallery.media.model.MediaStatus;

public record UploadFileResponse(String clientFileId, String fileName, long size, MediaStatus status,
		String detectedContentType, String checksumSha256, String errorCode) {
}
