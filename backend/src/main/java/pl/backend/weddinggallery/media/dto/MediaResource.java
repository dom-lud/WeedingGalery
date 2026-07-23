package pl.backend.weddinggallery.media.dto;

public record MediaResource(String objectKey, String fileName, String contentType, Long size) {
}
