package pl.backend.weddinggallery.media.dto;

import java.util.List;

public record GalleryDownload(String fileName, List<MediaResource> files) {
}
