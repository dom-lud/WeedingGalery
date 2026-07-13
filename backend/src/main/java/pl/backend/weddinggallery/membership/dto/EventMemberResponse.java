package pl.backend.weddinggallery.membership.dto;

import java.time.Instant;
import pl.backend.weddinggallery.membership.model.EventRole;

public record EventMemberResponse(String id, String userId, String email, EventRole role, Instant joinedAt) {
}
