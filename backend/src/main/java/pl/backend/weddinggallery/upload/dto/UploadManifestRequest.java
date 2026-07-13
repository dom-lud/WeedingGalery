package pl.backend.weddinggallery.upload.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record UploadManifestRequest(@NotEmpty @Size(max = 50) List<@Valid ManifestFile> files) {
	public record ManifestFile(@NotBlank @Size(max = 100) String clientFileId,
			@NotBlank @Size(max = 255) String fileName, @NotBlank @Size(max = 100) String declaredContentType,
			@Positive long size) {
	}
}
