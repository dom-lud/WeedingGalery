package pl.backend.weddinggallery.upload.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;
import pl.backend.weddinggallery.common.exception.*;
import pl.backend.weddinggallery.publicaccess.service.PublicRateLimiter;
import pl.backend.weddinggallery.upload.service.UploadService;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class PublicUploadPreflightFilter extends OncePerRequestFilter {
	private static final long MULTIPART_OVERHEAD_BYTES = 1024 * 1024;
	private final UploadService uploadService;
	private final PublicRateLimiter rateLimiter;
	private final ObjectMapper objectMapper;

	public PublicUploadPreflightFilter(UploadService uploadService, PublicRateLimiter rateLimiter,
			ObjectMapper objectMapper) {
		this.uploadService = uploadService;
		this.rateLimiter = rateLimiter;
		this.objectMapper = objectMapper;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String[] parts = request.getRequestURI().split("/");
		return !"PUT".equals(request.getMethod()) || parts.length != 9 || !"api".equals(parts[1])
				|| !"public".equals(parts[2]) || !"galleries".equals(parts[3]) || !"upload-sessions".equals(parts[5])
				|| !"files".equals(parts[7]);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String[] parts = request.getRequestURI().split("/");
		String address = clientAddress(request);
		try {
			rateLimiter.check(address + ":upload-file-global", 200);
			HttpSession session = request.getSession(false);
			if (session == null) {
				writeError(response, 404, "PUBLIC_GALLERY_NOT_FOUND", "Public gallery was not found.");
				return;
			}
			long expectedSize = uploadService.preflightUpload(parts[4], parts[6], parts[8], session);
			long contentLength = request.getContentLengthLong();
			if (contentLength < 0) {
				writeError(response, 411, "CONTENT_LENGTH_REQUIRED", "Content-Length is required for uploads.");
				return;
			}
			if (contentLength > expectedSize + MULTIPART_OVERHEAD_BYTES) {
				writeError(response, 413, "UPLOAD_FILE_TOO_LARGE", "The upload exceeds its declared size.");
				return;
			}
			chain.doFilter(request, response);
		} catch (RateLimitException ex) {
			response.setHeader("Retry-After", Long.toString(ex.getRetryAfterSeconds()));
			writeError(response, 429, "RATE_LIMIT_EXCEEDED", "Too many requests.");
		} catch (AppException ex) {
			writeError(response, ex.getErrorCode().getStatus().value(), ex.getErrorCode().name(), ex.getMessage());
		}
	}

	private void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		objectMapper.writeValue(response.getOutputStream(), ErrorResponse.of(code, message));
	}

	private String clientAddress(HttpServletRequest request) {
		String proxied = request.getHeader("X-Real-IP");
		return proxied == null || proxied.isBlank() ? request.getRemoteAddr() : proxied;
	}
}
