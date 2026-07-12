package pl.backend.weddinggallery.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.auth.dto.LoginRequest;
import pl.backend.weddinggallery.auth.dto.RegisterRequest;
import pl.backend.weddinggallery.auth.exception.AuthErrorCode;
import pl.backend.weddinggallery.auth.exception.AuthException;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuditService auditService;

	public void registerUser(RegisterRequest request) {
		String email = request.email().toLowerCase();
		log.info("Attempting to register user with email: {}", email);

		if (userRepository.findByEmail(email).isPresent()) {
			log.warn("Registration failed. Email already in use: {}", email);
			throw new AuthException(AuthErrorCode.EMAIL_ALREADY_IN_USE);
		}

		User user = new User();
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		userRepository.save(user);

		log.info("User registered successfully: {}", email);
		auditService.logEvent(email, "USER_REGISTERED", "New user account created");
	}

	public String loginUser(LoginRequest request, HttpServletRequest httpRequest) {
		String email = request.email().toLowerCase();
		log.info("Attempting to log in user: {}", email);

		UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(email,
				request.password());

		// This will throw BadCredentialsException (handled by AuthExceptionHandler) if
		// auth fails
		Authentication auth = authenticationManager.authenticate(authReq);

		SecurityContext sc = SecurityContextHolder.getContext();
		sc.setAuthentication(auth);
		HttpSession session = httpRequest.getSession(true);
		session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

		log.info("User logged in successfully: {}", email);
		auditService.logEvent(email, "USER_LOGGED_IN", "User session created");

		return auth.getName();
	}
}
