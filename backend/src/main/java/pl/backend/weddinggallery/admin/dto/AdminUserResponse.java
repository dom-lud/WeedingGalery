package pl.backend.weddinggallery.admin.dto;

import java.time.LocalDateTime;
import pl.backend.weddinggallery.user.model.SystemRole;

public record AdminUserResponse(String id, String email, SystemRole systemRole, boolean locked,
		LocalDateTime lockedUntil, LocalDateTime createdAt) {
}
