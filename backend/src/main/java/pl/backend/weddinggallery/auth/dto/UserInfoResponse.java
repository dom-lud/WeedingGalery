package pl.backend.weddinggallery.auth.dto;

import pl.backend.weddinggallery.user.model.SystemRole;

public record UserInfoResponse(String email, SystemRole systemRole) {
	public UserInfoResponse(String email) {
		this(email, null);
	}
}
