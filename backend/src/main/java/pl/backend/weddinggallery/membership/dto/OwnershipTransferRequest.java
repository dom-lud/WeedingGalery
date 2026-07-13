package pl.backend.weddinggallery.membership.dto;

import jakarta.validation.constraints.NotBlank;

public record OwnershipTransferRequest(@NotBlank String targetMembershipId) {
}
