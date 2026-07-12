package pl.backend.weddinggallery.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.web.client.RestTemplate;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthControllerTest {

	@LocalServerPort
	private int port;

	private final RestTemplate restTemplate = new RestTemplate();

	@BeforeEach
	void setUp() {
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
		// Fetch CSRF token by doing a GET to a public endpoint
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
		headers.add("Content-Type", "application/json");
		return headers;
	}

	@Test
	void shouldRegisterAndLoginSuccessfully() {
		HttpHeaders headers = getHeadersWithCsrf();

		// 1. Register
		Map<String, String> registerRequest = Map.of("email", "test@example.com", "password", "password123");
		HttpEntity<Map<String, String>> registerEntity = new HttpEntity<>(registerRequest, headers);

		ResponseEntity<String> regResponse = restTemplate.exchange(getBaseUrl() + "/register", HttpMethod.POST,
				registerEntity, String.class);

		assertThat(regResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		// 2. Login
		Map<String, String> loginRequest = Map.of("email", "test@example.com", "password", "password123");
		HttpEntity<Map<String, String>> loginEntity = new HttpEntity<>(loginRequest, headers);

		ResponseEntity<String> loginResponse = restTemplate.exchange(getBaseUrl() + "/login", HttpMethod.POST,
				loginEntity, String.class);

		assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		// 3. Me with session
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
	void shouldFailRegistrationWhenEmailExists() {
		HttpHeaders headers = getHeadersWithCsrf();
		Map<String, String> registerRequest = Map.of("email", "existing@example.com", "password", "password123");
		HttpEntity<Map<String, String>> entity = new HttpEntity<>(registerRequest, headers);

		// First registration
		restTemplate.exchange(getBaseUrl() + "/register", HttpMethod.POST, entity, String.class);

		// Second registration
		ResponseEntity<String> regResponse2 = restTemplate.exchange(getBaseUrl() + "/register", HttpMethod.POST, entity,
				String.class);

		assertThat(regResponse2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(regResponse2.getBody()).contains("EMAIL_ALREADY_IN_USE");
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
}
