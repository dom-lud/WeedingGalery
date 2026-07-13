package pl.backend.weddinggallery.membership.exception;

import org.springframework.http.HttpStatus;
import pl.backend.weddinggallery.common.exception.ErrorCode;

public enum MembershipErrorCode implements ErrorCode {
	MEMBERSHIP_NOT_FOUND(HttpStatus.NOT_FOUND, "Membership was not found."), MEMBERSHIP_ALREADY_ACTIVE(
			HttpStatus.CONFLICT,
			"User is already an active event manager."), INVALID_MEMBERSHIP_TARGET(HttpStatus.BAD_REQUEST,
					"Membership target is invalid."), USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User was not found.");

	private final HttpStatus status;
	private final String message;

	MembershipErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	@Override
	public HttpStatus getStatus() {
		return status;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
