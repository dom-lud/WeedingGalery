package pl.backend.weddinggallery.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.service.AuditService;

@Component
@RequiredArgsConstructor
public class AuditLogoutSuccessHandler implements LogoutSuccessHandler {

	private final AuditService auditService;

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		if (authentication != null && authentication.getName() != null
				&& !"anonymousUser".equals(authentication.getName())) {
			auditService.logEvent(authentication.getName(), EventType.USER_LOGGED_OUT, "User session terminated");
		}
		response.setStatus(HttpStatus.OK.value());
	}
}
