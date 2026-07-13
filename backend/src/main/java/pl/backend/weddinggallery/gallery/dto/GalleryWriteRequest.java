package pl.backend.weddinggallery.gallery.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GalleryWriteRequest(@NotBlank @Size(max = 255) String name, @Size(max = 5000) String description,
		@Min(0) @Max(100000) int sortOrder) {
}
