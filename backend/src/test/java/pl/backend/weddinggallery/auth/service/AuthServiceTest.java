package pl.backend.weddinggallery.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.auth.dto.LoginRequest;
import pl.backend.weddinggallery.auth.dto.RegisterRequest;
import pl.backend.weddinggallery.auth.exception.AuthErrorCode;
import pl.backend.weddinggallery.auth.exception.AuthException;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
	@Mock
	AuthenticationManager authenticationManager;
	@Mock
	UserRepository users;
	@Mock
	PasswordEncoder passwords;
	@Mock
	AuditService audit;
	@Mock
	Authentication authentication;
	private AuthService service;

	@BeforeEach
	void setUp() {
		service = new AuthService(authenticationManager, users, passwords, audit);
	}

	@Test
	void registersNormalizedNewUserAndRejectsDuplicate() {
		when(users.findByEmail("new@example.com")).thenReturn(Optional.empty());
		when(passwords.encode("strong-password")).thenReturn("encoded");
		service.registerUser(new RegisterRequest("NEW@EXAMPLE.COM", "strong-password"));
		verify(users).save(argThat(user -> user.getEmail().equals("new@example.com")
				&& user.getPasswordHash().equals("encoded")));
		verify(audit).logEvent("new@example.com", EventType.USER_REGISTERED, "New user account created");

		when(users.findByEmail("new@example.com")).thenReturn(Optional.of(User.builder().build()));
		assertThatThrownBy(() -> service.registerUser(new RegisterRequest("new@example.com", "strong-password")))
				.isInstanceOfSatisfying(AuthException.class,
					ex -> assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.EMAIL_ALREADY_IN_USE));
	}

	@Test
	void blocksCurrentlyLockedAccountBeforeAuthenticating() {
		User user = User.builder().email("user@example.com").lockedUntil(LocalDateTime.now().plusMinutes(5)).build();
		when(users.findByEmail("user@example.com")).thenReturn(Optional.of(user));
		assertThatThrownBy(
				() -> service.loginUser(new LoginRequest("user@example.com", "password"), new MockHttpServletRequest()))
				.isInstanceOfSatisfying(AuthException.class,
						ex -> assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.ACCOUNT_LOCKED));
		verifyNoInteractions(authenticationManager);
	}

	@Test
	void recordsBothFailedLoginBranchesAndLocksAtThreshold() {
		User user = User.builder().email("user@example.com").failedLoginAttempts(0).build();
		when(users.findByEmail("user@example.com")).thenReturn(Optional.of(user));
		when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
		for (int attempt = 1; attempt <= 5; attempt++) {
			assertThatThrownBy(() -> service.loginUser(new LoginRequest("user@example.com", "password"),
					new MockHttpServletRequest())).isInstanceOf(BadCredentialsException.class);
		}
		assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
		assertThat(user.getLockedUntil()).isAfter(LocalDateTime.now());
		verify(users, atLeast(5)).save(user);
		verify(audit, atLeastOnce()).logEvent(eq("user@example.com"), eq(EventType.USER_LOGIN_BLOCKED), anyString());
	}

	@Test
	void successfulLoginResetsTrackingOnlyWhenNeeded() {
		User user = User.builder().email("user@example.com").failedLoginAttempts(2)
				.lockedUntil(LocalDateTime.now().minusMinutes(1)).build();
		when(users.findByEmail("user@example.com")).thenReturn(Optional.of(user));
		when(authenticationManager.authenticate(any())).thenReturn(authentication);
		when(authentication.getName()).thenReturn("user@example.com");
		assertThat(service.loginUser(new LoginRequest("user@example.com", "password"), new MockHttpServletRequest()))
				.isEqualTo("user@example.com");
		assertThat(user.getFailedLoginAttempts()).isZero();
		assertThat(user.getLockedUntil()).isNull();
		verify(users).save(user);
		verify(audit).logEvent("user@example.com", EventType.USER_LOGGED_IN, "User session created");
	}
}
