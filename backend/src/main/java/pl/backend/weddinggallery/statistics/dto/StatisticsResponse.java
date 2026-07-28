package pl.backend.weddinggallery.statistics.dto;

public record StatisticsResponse(String eventId, String galleryId, long mediaCount, long imageCount, long videoCount,
		long uploadSessionCount, long uploadFileCount, long storageUsedBytes, long processingFailureCount,
		long publicViewCount, long downloadCount, long usageEventCount) {
}
