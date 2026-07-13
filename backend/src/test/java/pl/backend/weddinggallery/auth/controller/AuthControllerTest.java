package pl.backend.weddinggallery.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.repository.AuditEventRepository;
import pl.backend.weddinggallery.user.model.SystemRole;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.user.repository.UserRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "app.security.csrf.enabled=true")
@ActiveProfiles("test")
public class AuthControllerTest {

	@LocalServerPort
	private int port;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuditEventRepository auditEventRepository;

	private final RestTemplate restTemplate = new RestTemplate();

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
		auditEventRepository.deleteAll();
		restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
			@Override
			public boolean hasError(ClientHttpResponse response) {
				return false;
			}
		});
	}

	private String getBaseUrl() {
		return "http://localhost:" + port + "/api/auth";
	}

	private HttpHeaders getHeadersWithCsrf() {
		ResponseEntity<String> initResponse = restTemplate.getForEntity("http://localhost:" + port + "/api/auth/csrf",
				String.class);
		HttpHeaders headers = new HttpHeaders();
		List<String> cookies = initResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
		if (cookies != null) {
			for (String cookie : cookies) {
				headers.add(HttpHeaders.COOKIE, cookie);
				if (cookie.startsWith("XSRF-TOKEN=")) {
					String token = cookie.split(";")[0].substring("XSRF-TOKEN=".length());
					headers.add("X-XSRF-TOKEN", token);
				}
			}
		}
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
		return headers;
	}

	private void createUser(String email, String password, SystemRole role) {
		User user = User.builder().email(email.toLowerCase()).passwordHash(passwordEncoder.encode(password))
				.systemRole(role).failedLoginAttempts(0).lockedUntil(null).createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).build();
		userRepository.save(user);
	}

	private HttpHeaders loginAndGetSessionHeaders(String email, String password) {
		HttpHeaders headers = getHeadersWithCsrf();
		Map<String, String> loginRequest = Map.of("email", email, "password", password);
		HttpEntity<Map<String, String>> loginEntity = new HttpEntity<>(loginRequest, headers);

		ResponseEntity<String> loginResponse = restTemplate.exchange(getBaseUrl() + "/login", HttpMethod.POST,
				loginEntity, String.class);

		assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		HttpHeaders sessionHeaders = new HttpHeaders();
		List<String> loginCookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
		if (loginCookies != null) {
			for (String cookie : loginCookies) {
				sessionHeaders.add(HttpHeaders.COOKIE, cookie);
			}
		}
		sessionHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
		return sessionHeaders;
	}

	private HttpHeaders getAuthenticatedHeaders(String email, String password) {
		HttpHeaders csrfHeaders = getHeadersWithCsrf();
		HttpHeaders sessionHeaders = loginAndGetSessionHeaders(email, password);
		List<String> csrfCookies = csrfHeaders.get(HttpHeaders.COOKIE);
		if (csrfCookies != null) {
			for (String cookie : csrfCookies) {
				sessionHeaders.add(HttpHeaders.COOKIE, cookie);
			}
		}
		sessionHeaders.add("X-XSRF-TOKEN", csrfHeaders.getFirst("X-XSRF-TOKEN"));
		return sessionHeaders;
	}

	@Test
	void shouldAllowAdminToRegisterAndNewUserToLoginSuccessfully() {
		createUser("admin@example.com", "password123", SystemRole.ADMIN);
		HttpHeaders adminHeaders = getAuthenticatedHeaders("admin@example.com", "password123");

		Map<String, String> registerRequest = Map.of("email", "test@example.com", "password", "password123");
		HttpEntity<Map<String, String>> registerEntity = new HttpEntity<>(registerRequest, adminHeaders);

		ResponseEntity<String> regResponse = restTemplate.exchange(getBaseUrl() + "/register", HttpMethod.POST,
				registerEntity, String.class);

		assertThat(regResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		HttpHeaders loginHeaders = getHeadersWithCsrf();
		Map<String, String> loginRequest = Map.of("email", "test@example.com", "password", "password123");
		HttpEntity<Map<String, String>> loginEntity = new HttpEntity<>(loginRequest, loginHeaders);

		ResponseEntity<String> loginResponse = restTemplate.exchange(getBaseUrl() + "/login", HttpMethod.POST,
				loginEntity, String.class);

		assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		HttpHeaders sessionHeaders = new HttpHeaders();
		List<String> loginCookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
		if (loginCookies != null) {
			for (String cookie : loginCookies) {
				sessionHeaders.add(HttpHeaders.COOKIE, cookie);
			}
		}
		HttpEntity<Void> meEntity = new HttpEntity<>(null, sessionHeaders);
		ResponseEntity<String> meResponse = restTemplate.exchange(getBaseUrl() + "/me", HttpMethod.GET, meEntity,
				String.class);

		assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(meResponse.getBody()).contains("test@example.com");
	}

	@Test
	void shouldRejectRegistrationWithoutAuthenticatedAdmin() {
		HttpHeaders headers = getHeadersWithCsrf();
		Map<String, String> registerRequest = Map.of("email", "blocked@example.com", "password", "password123");
		HttpEntity<Map<String, String>> entity = new HttpEntity<>(registerRequest, headers);

		ResponseEntity<String> regResponse = restTemplate.exchange(getBaseUrl() + "/register", HttpMethod.POST, entity,
				String.class);

		assertThat(regResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldRejectRegistrationWhenCsrfTokenIsMissing() {
		createUser("admin@example.com", "password123", SystemRole.ADMIN);
		HttpHeaders adminHeaders = loginAndGetSessionHeaders("admin@example.com", "password123");

		Map<String, String> registerRequest = Map.of("email", "blocked@example.com", "password", "password123");
		HttpEntity<Map<String, String>> entity = new HttpEntity<>(registerRequest, adminHeaders);

		ResponseEntity<String> regResponse = restTemplate.exchange(getBaseUrl() + "/register", HttpMethod.POST, entity,
				String.class);

		assertThat(regResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldRejectRegistrationForAuthenticatedNonAdminUser() {
		createUser("user@example.com", "password123", SystemRole.USER);
		HttpHeaders userHeaders = getAuthenticatedHeaders("user@example.com", "password123");

		Map<String, String> registerRequest = Map.of("email", "blocked@example.com", "password", "password123");
		HttpEntity<Map<String, String>> entity = new HttpEntity<>(registerRequest, userHeaders);

		ResponseEntity<String> regResponse = restTemplate.exchange(getBaseUrl() + "/register", HttpMethod.POST, entity,
				String.class);

		assertThat(regResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldFailRegistrationWhenEmailExists() {
		createUser("admin@example.com", "password123", SystemRole.ADMIN);
		createUser("existing@example.com", "password123", SystemRole.USER);
		HttpHeaders adminHeaders = getAuthenticatedHeaders("admin@example.com", "password123");

		Map<String, String> registerRequest = Map.of("email", "existing@example.com", "password", "password123");
		HttpEntity<Map<String, String>> entity = new HttpEntity<>(registerRequest, adminHeaders);

		ResponseEntity<String> regResponse = restTemplate.exchange(getBaseUrl() + "/register", HttpMethod.POST, entity,
				String.class);

		assertThat(regResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(regResponse.getBody()).contains("EMAIL_ALREADY_IN_USE");
	}

	@Test
	void shouldRejectRegistrationWhenPayloadIsInvalid() {
		createUser("admin@example.com", "password123", SystemRole.ADMIN);
		HttpHeaders adminHeaders = getAuthenticatedHeaders("admin@example.com", "password123");

		Map<String, String> registerRequest = Map.of("email", "not-an-email", "password", "short");
		HttpEntity<Map<String, String>> entity = new HttpEntity<>(registerRequest, adminHeaders);

		ResponseEntity<String> regResponse = restTemplate.exchange(getBaseUrl() + "/register", HttpMethod.POST, entity,
				String.class);

		assertThat(regResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(regResponse.getBody()).contains("VALIDATION_ERROR");
	}

	@Test
	void shouldNormalizeEmailDuringRegistrationAndLogin() {
		createUser("admin@example.com", "password123", SystemRole.ADMIN);
		HttpHeaders adminHeaders = getAuthenticatedHeaders("admin@example.com", "password123");

		Map<String, String> registerRequest = Map.of("email", "MixedCaseUser@Example.com", "password", "password123");
		HttpEntity<Map<String, String>> registerEntity = new HttpEntity<>(registerRequest, adminHeaders);

		ResponseEntity<String> regResponse = restTemplate.exchange(getBaseUrl() + "/register", HttpMethod.POST,
				registerEntity, String.class);

		assertThat(regResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(userRepository.findByEmail("mixedcaseuser@example.com")).isPresent();

		HttpHeaders loginHeaders = getHeadersWithCsrf();
		Map<String, String> loginRequest = Map.of("email", "MIXEDCASEUSER@EXAMPLE.COM", "password", "password123");
		HttpEntity<Map<String, String>> loginEntity = new HttpEntity<>(loginRequest, loginHeaders);

		ResponseEntity<String> loginResponse = restTemplate.exchange(getBaseUrl() + "/login", HttpMethod.POST,
				loginEntity, String.class);

		assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(loginResponse.getBody()).contains("mixedcaseuser@example.com");
	}

	@Test
	void shouldFailLoginWithBadCredentials() {
		HttpHeaders headers = getHeadersWithCsrf();
		Map<String, String> loginRequest = Map.of("email", "wrong@example.com", "password", "wrongpass");
		HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);

		ResponseEntity<String> loginResponse = restTemplate.exchange(getBaseUrl() + "/login", HttpMethod.POST, entity,
				String.class);

		assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldRejectLoginWhenPayloadIsInvalid() {
		HttpHeaders headers = getHeadersWithCsrf();
		Map<String, String> loginRequest = Map.of("email", "not-an-email", "password", "");
		HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);

		ResponseEntity<String> loginResponse = restTemplate.exchange(getBaseUrl() + "/login", HttpMethod.POST, entity,
				String.class);

		assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(loginResponse.getBody()).contains("VALIDATION_ERROR");
	}

	@Test
	void shouldReturnUnauthorizedForCurrentUserWithoutSession() {
		ResponseEntity<String> meResponse = restTemplate.exchange(getBaseUrl() + "/me", HttpMethod.GET, null,
				String.class);

		assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldLogoutInvalidateSessionAndAuditLogout() {
		createUser("admin@example.com", "password123", SystemRole.ADMIN);
		HttpHeaders authenticatedHeaders = getAuthenticatedHeaders("admin@example.com", "password123");

		ResponseEntity<String> logoutResponse = restTemplate.exchange(getBaseUrl() + "/logout", HttpMethod.POST,
				new HttpEntity<>(null, authenticatedHeaders), String.class);

		assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		ResponseEntity<String> meResponse = restTemplate.exchange(getBaseUrl() + "/me", HttpMethod.GET,
				new HttpEntity<>(null, authenticatedHeaders), String.class);

		assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(auditEventRepository.findAll()).extracting("eventType").contains(EventType.USER_LOGGED_OUT);
	}

	@Test
	void shouldLockAccountAfterFiveFailedAttemptsAndRejectCorrectPasswordWhileLocked() {
		createUser("locked@example.com", "password123", SystemRole.USER);

		for (int attempt = 1; attempt <= 5; attempt++) {
			HttpHeaders headers = getHeadersWithCsrf();
			Map<String, String> loginRequest = Map.of("email", "locked@example.com", "password", "wrongpass");
			HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);

			ResponseEntity<String> loginResponse = restTemplate.exchange(getBaseUrl() + "/login", HttpMethod.POST,
					entity, String.class);

			assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		}

		User lockedUser = userRepository.findByEmail("locked@example.com").orElseThrow();
		assertThat(lockedUser.getFailedLoginAttempts()).isEqualTo(5);
		assertThat(lockedUser.getLockedUntil()).isNotNull();

		HttpHeaders headers = getHeadersWithCsrf();
		Map<String, String> loginRequest = Map.of("email", "locked@example.com", "password", "password123");
		HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);

		ResponseEntity<String> loginResponse = restTemplate.exchange(getBaseUrl() + "/login", HttpMethod.POST, entity,
				String.class);

		assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(auditEventRepository.findAll()).extracting("eventType")
				.contains(EventType.USER_LOGIN_FAILED, EventType.USER_LOGIN_BLOCKED);
	}
}
