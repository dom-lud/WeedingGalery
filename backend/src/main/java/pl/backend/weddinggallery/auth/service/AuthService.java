package pl.backend.weddinggallery.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import pl.backend.weddinggallery.audit.model.EventType;
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

	private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
	private static final long ACCOUNT_LOCK_MINUTES = 15;

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
		user.setFailedLoginAttempts(0);
		user.setLockedUntil(null);
		userRepository.save(user);

		log.info("User registered successfully: {}", email);
		auditService.logEvent(email, EventType.USER_REGISTERED, "New user account created");
	}

	public String loginUser(LoginRequest request, HttpServletRequest httpRequest) {
		String email = request.email().toLowerCase();
		log.info("Attempting to log in user: {}", email);
		enforceAccountNotLocked(email);

		UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(email,
				request.password());

		Authentication auth;
		try {
			auth = authenticationManager.authenticate(authReq);
		} catch (BadCredentialsException ex) {
			handleFailedLogin(email);
			throw ex;
		}

		SecurityContext sc = SecurityContextHolder.getContext();
		sc.setAuthentication(auth);
		HttpSession session = httpRequest.getSession(true);
		session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
		resetFailedLoginTracking(email);

		log.info("User logged in successfully: {}", email);
		auditService.logEvent(email, EventType.USER_LOGGED_IN, "User session created");

		return auth.getName();
	}

	private void enforceAccountNotLocked(String email) {
		userRepository.findByEmail(email).ifPresent(user -> {
			LocalDateTime lockedUntil = user.getLockedUntil();
			if (lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now())) {
				log.warn("Blocked login attempt for locked account: {}", email);
				auditService.logEvent(email, EventType.USER_LOGIN_BLOCKED,
						"Login attempt blocked until " + lockedUntil.truncatedTo(ChronoUnit.SECONDS));
				throw new AuthException(AuthErrorCode.ACCOUNT_LOCKED);
			}
		});
	}

	private void handleFailedLogin(String email) {
		userRepository.findByEmail(email).ifPresent(user -> {
			int failedAttempts = user.getFailedLoginAttempts() + 1;
			user.setFailedLoginAttempts(failedAttempts);

			if (failedAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
				LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(ACCOUNT_LOCK_MINUTES);
				user.setLockedUntil(lockedUntil);
				log.warn("Account locked after failed login attempts: {}", email);
				auditService.logEvent(email, EventType.USER_LOGIN_BLOCKED,
						"Account locked until " + lockedUntil.truncatedTo(ChronoUnit.SECONDS));
			} else {
				log.warn("Failed login attempt {} for user: {}", failedAttempts, email);
			}

			userRepository.save(user);
			auditService.logEvent(email, EventType.USER_LOGIN_FAILED, "Invalid credentials");
		});
	}

	private void resetFailedLoginTracking(String email) {
		userRepository.findByEmail(email).ifPresent(user -> {
			if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
				user.setFailedLoginAttempts(0);
				user.setLockedUntil(null);
				userRepository.save(user);
			}
		});
	}
}
