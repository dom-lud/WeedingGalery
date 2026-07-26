package pl.backend.weddinggallery.media.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.security.Principal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.media.dto.MediaModerationAction;
import pl.backend.weddinggallery.media.dto.MediaModerationRequest;
import pl.backend.weddinggallery.media.exception.MediaModerationErrorCode;
import pl.backend.weddinggallery.media.service.MediaModerationService;

@ExtendWith(MockitoExtension.class)
class MediaModerationControllerTest {
	@Mock
	MediaModerationService service;
	@Mock
	Principal principal;

	@Test
	void rejectAndHideRequireNonBlankReason() {
		var controller = new MediaModerationController(service);
		assertThatThrownBy(
				() -> controller.reject("event", "gallery", "media", new MediaModerationRequest(" "), principal))
				.isInstanceOfSatisfying(AppException.class, e -> assertThat(e.getErrorCode())
						.isEqualTo(MediaModerationErrorCode.MEDIA_MODERATION_REASON_REQUIRED));
		assertThatThrownBy(
				() -> controller.hide("event", "gallery", "media", new MediaModerationRequest(null), principal))
				.isInstanceOfSatisfying(AppException.class, e -> assertThat(e.getErrorCode())
						.isEqualTo(MediaModerationErrorCode.MEDIA_MODERATION_REASON_REQUIRED));
		verifyNoInteractions(service);
	}

	@Test
    void approveAndRestoreAllowOptionalRequestAndForwardPrincipal() {
        when(principal.getName()).thenReturn("owner@example.com");
        var controller = new MediaModerationController(service);

        controller.approve("event", "gallery", "media", null, principal);
        controller.restore("event", "gallery", "media", new MediaModerationRequest(null), principal);

        verify(service).moderate("event", "gallery", "media", MediaModerationAction.APPROVE, null, "owner@example.com");
        verify(service).moderate("event", "gallery", "media", MediaModerationAction.RESTORE, null, "owner@example.com");
    }
}
