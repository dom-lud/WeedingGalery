package pl.backend.weddinggallery.admin.dto;

public record AdminDashboardResponse(long users, long lockedUsers, long events, long galleries, long media,
		long storageUsedBytes, long auditEvents) {
}
