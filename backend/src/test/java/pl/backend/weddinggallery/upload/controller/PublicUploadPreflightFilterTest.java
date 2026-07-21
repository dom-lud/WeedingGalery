package pl.backend.weddinggallery.upload.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import pl.backend.weddinggallery.common.exception.*;
import pl.backend.weddinggallery.publicaccess.service.PublicRateLimiter;
import pl.backend.weddinggallery.upload.exception.UploadErrorCode;
import pl.backend.weddinggallery.upload.service.UploadService;
import tools.jackson.databind.ObjectMapper;

class PublicUploadPreflightFilterTest {
	private UploadService uploadService;
	private PublicRateLimiter rateLimiter;
	private PublicUploadPreflightFilter filter;

	@BeforeEach
	void setUp() {
		uploadService = mock(UploadService.class);
		rateLimiter = mock(PublicRateLimiter.class);
		filter = new PublicUploadPreflightFilter(uploadService, rateLimiter, new ObjectMapper());
	}

	@Test
	void bypassesEveryNonUploadRouteShapeWithoutApplyingRateLimit() throws Exception {
		for (String[] route : new String[][]{{"GET", validUri()}, {"POST", validUri()},
				{"PUT", "/api/public/galleries/g/upload-sessions/s/files"},
				{"PUT", "/api/private/galleries/g/upload-sessions/s/files/f"},
				{"PUT", "/api/public/albums/g/upload-sessions/s/files/f"},
				{"PUT", "/api/public/galleries/g/sessions/s/files/f"},
				{"PUT", "/api/public/galleries/g/upload-sessions/s/media/f"}}) {
			MockHttpServletRequest request = new MockHttpServletRequest(route[0], route[1]);
			MockHttpServletResponse response = new MockHttpServletResponse();
			FilterChain chain = mock(FilterChain.class);
			filter.doFilter(request, response, chain);
			verify(chain).doFilter(request, response);
		}
		verifyNoInteractions(rateLimiter, uploadService);
	}

	@Test
	void rejectsMissingSessionAndUsesTrustedProxyOrRemoteAddressInSecurityBucket() throws Exception {
		MockHttpServletRequest proxied = request(-1, false);
		proxied.addHeader("X-Real-IP", " 203.0.113.4 ");
		MockHttpServletResponse response = run(proxied);
		assertError(response, 404, "PUBLIC_GALLERY_NOT_FOUND");
		verify(rateLimiter).check(" 203.0.113.4 :upload-file-global", 200);

		MockHttpServletRequest direct = request(-1, false);
		direct.addHeader("X-Real-IP", "  ");
		run(direct);
		verify(rateLimiter).check("127.0.0.1:upload-file-global", 200);
	}

	@Test
	void enforcesUnknownAndDeclaredLengthBoundariesBeforeReadingMultipartBody() throws Exception {
		MockHttpServletRequest unknown = request(-1, true);
		when(uploadService.preflightUpload(eq("gallery"), eq("session"), eq("file"), any(HttpSession.class)))
				.thenReturn(10L);
		assertError(run(unknown), 411, "CONTENT_LENGTH_REQUIRED");

		long maximumAccepted = 10L + 1024 * 1024;
		MockHttpServletRequest exact = request(maximumAccepted, true);
		FilterChain exactChain = mock(FilterChain.class);
		MockHttpServletResponse exactResponse = new MockHttpServletResponse();
		filter.doFilter(exact, exactResponse, exactChain);
		verify(exactChain).doFilter(exact, exactResponse);

		MockHttpServletRequest over = request(maximumAccepted + 1, true);
		assertError(run(over), 413, "UPLOAD_FILE_TOO_LARGE");
	}

	@Test
	void translatesRateLimitAndDomainFailuresWithoutInvokingTheController() throws Exception {
		doThrow(new RateLimitException(17)).when(rateLimiter).check(anyString(), eq(200));
		MockHttpServletResponse limited = run(request(1, true));
		assertError(limited, 429, "RATE_LIMIT_EXCEEDED");
		assertThat(limited.getHeader("Retry-After")).isEqualTo("17");
		verifyNoInteractions(uploadService);

		reset(rateLimiter);
		when(uploadService.preflightUpload(anyString(), anyString(), anyString(), any(HttpSession.class)))
				.thenThrow(new AppException(UploadErrorCode.UPLOAD_SESSION_NOT_FOUND));
		MockHttpServletResponse domainError = run(request(1, true));
		assertError(domainError, UploadErrorCode.UPLOAD_SESSION_NOT_FOUND.getStatus().value(),
				"UPLOAD_SESSION_NOT_FOUND");
	}

	private MockHttpServletRequest request(long contentLength, boolean session) {
		MockHttpServletRequest request = spy(new MockHttpServletRequest("PUT", validUri()));
		request.setRemoteAddr("127.0.0.1");
		doReturn(contentLength).when(request).getContentLengthLong();
		if (session)
			request.getSession(true);
		return request;
	}

	private MockHttpServletResponse run(MockHttpServletRequest request) throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request, response, mock(FilterChain.class));
		return response;
	}

	private void assertError(MockHttpServletResponse response, int status, String code) throws Exception {
		assertThat(response.getStatus()).isEqualTo(status);
		assertThat(response.getContentType()).isEqualTo("application/json");
		assertThat(response.getContentAsString()).contains("\"code\":\"" + code + "\"");
	}

	private String validUri() {
		return "/api/public/galleries/gallery/upload-sessions/session/files/file";
	}
}
