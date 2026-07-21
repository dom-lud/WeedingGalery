package pl.backend.weddinggallery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.auth.controller.AuthController;
import pl.backend.weddinggallery.auth.service.AuthService;
import pl.backend.weddinggallery.publicaccess.controller.PublicGalleryController;
import pl.backend.weddinggallery.publicaccess.dto.*;
import pl.backend.weddinggallery.publicaccess.service.*;
import pl.backend.weddinggallery.security.handler.AuditLogoutSuccessHandler;
import pl.backend.weddinggallery.upload.controller.PublicUploadController;
import pl.backend.weddinggallery.upload.dto.*;
import pl.backend.weddinggallery.upload.service.UploadService;

class ControllerBoundaryTest {

	@Test
	void publicGalleryUsesProxyOnlyWhenPresentAndNonBlank() {
		GalleryAccessService service = mock(GalleryAccessService.class);
		PublicRateLimiter limiter = mock(PublicRateLimiter.class);
		PublicGalleryController controller = new PublicGalleryController(service, limiter);
		PublicAccessRequest body = new PublicAccessRequest("12345678", null);
		MockHttpSession session = new MockHttpSession();
		PublicGalleryResponse expected = mock(PublicGalleryResponse.class);
		when(service.exchange("slug", body, session)).thenReturn(expected);

		for (String proxy : new String[]{null, "", "  "}) {
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setRemoteAddr("198.51.100.1");
			if (proxy != null)
				request.addHeader("X-Real-IP", proxy);
			assertThat(controller.access("slug", body, request, session)).isSameAs(expected);
		}
		MockHttpServletRequest proxied = new MockHttpServletRequest();
		proxied.addHeader("X-Real-IP", "203.0.113.5");
		controller.access("slug", body, proxied, session);

		verify(limiter, times(3)).check("198.51.100.1:access-global", 30);
		verify(limiter, times(3)).check("198.51.100.1:access:slug");
		verify(limiter).check("203.0.113.5:access-global", 30);
		verify(limiter).check("203.0.113.5:access:slug");
		controller.get("slug", session);
		verify(service).getPublic("slug", session);
	}

	@Test
	void uploadControllerDistinguishesReplayFromCreationAndAllClientAddressSources() {
		UploadService service = mock(UploadService.class);
		PublicRateLimiter limiter = mock(PublicRateLimiter.class);
		PublicUploadController controller = new PublicUploadController(service, limiter);
		UploadManifestRequest request = mock(UploadManifestRequest.class);
		UploadSessionResponse response = mock(UploadSessionResponse.class);
		when(response.id()).thenReturn("session");
		MockHttpSession session = new MockHttpSession();
		MockHttpServletRequest direct = new MockHttpServletRequest();
		direct.setRemoteAddr("198.51.100.2");

		when(service.create("slug", "valid-key", request, session)).thenReturn(
				new UploadService.CreateResult(response, false), new UploadService.CreateResult(response, true));
		assertThat(controller.create("slug", "valid-key", request, session, direct).getStatusCode())
				.isEqualTo(HttpStatus.OK);
		assertThat(controller.create("slug", "valid-key", request, session, direct).getStatusCode())
				.isEqualTo(HttpStatus.CREATED);

		for (String proxy : new String[]{"", "  ", "203.0.113.6"}) {
			MockHttpServletRequest addressed = new MockHttpServletRequest();
			addressed.setRemoteAddr("198.51.100.2");
			addressed.addHeader("X-Real-IP", proxy);
			when(service.create("slug", proxy + "-key", request, session))
					.thenReturn(new UploadService.CreateResult(response, false));
			controller.create("slug", proxy + "-key", request, session, addressed);
		}
		verify(limiter, times(4)).check("198.51.100.2:upload-session:slug", 30);
		verify(limiter).check("203.0.113.6:upload-session:slug", 30);
	}

	@Test
	void authenticationAndCsrfEndpointsCoverAnonymousInvalidAndAuthenticatedPrincipals() {
		AuthController controller = new AuthController(mock(AuthService.class));
		assertThat(controller.getCurrentUser(null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		Authentication unauthenticated = mock(Authentication.class);
		when(unauthenticated.isAuthenticated()).thenReturn(false);
		assertThat(controller.getCurrentUser(unauthenticated).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		Authentication anonymous = mock(Authentication.class);
		when(anonymous.isAuthenticated()).thenReturn(true);
		when(anonymous.getPrincipal()).thenReturn("anonymousUser");
		assertThat(controller.getCurrentUser(anonymous).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		Authentication user = mock(Authentication.class);
		when(user.isAuthenticated()).thenReturn(true);
		when(user.getPrincipal()).thenReturn(new Object());
		when(user.getName()).thenReturn("user@example.com");
		assertThat(controller.getCurrentUser(user).getBody().email()).isEqualTo("user@example.com");

		assertThat(controller.getCsrf(null).getStatusCode()).isEqualTo(HttpStatus.OK);
		CsrfToken csrf = mock(CsrfToken.class);
		controller.getCsrf(csrf);
		verify(csrf).getToken();
	}

	@Test
	void logoutAuditsOnlyConcreteAuthenticatedNamesAndAlwaysReturnsOk() throws Exception {
		AuditService audit = mock(AuditService.class);
		AuditLogoutSuccessHandler handler = new AuditLogoutSuccessHandler(audit);
		MockHttpServletRequest request = new MockHttpServletRequest();

		handler.onLogoutSuccess(request, new MockHttpServletResponse(), null);
		for (String name : new String[]{null, "anonymousUser", "user@example.com"}) {
			Authentication authentication = mock(Authentication.class);
			when(authentication.getName()).thenReturn(name);
			MockHttpServletResponse response = new MockHttpServletResponse();
			handler.onLogoutSuccess(request, response, authentication);
			assertThat(response.getStatus()).isEqualTo(200);
		}
		verify(audit).logEvent("user@example.com", EventType.USER_LOGGED_OUT, "User session terminated");
	}
}
