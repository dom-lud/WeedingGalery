package pl.backend.weddinggallery.event.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.repository.AuditEventRepository;
import pl.backend.weddinggallery.event.repository.EventRepository;
import pl.backend.weddinggallery.membership.repository.EventMembershipRepository;
import pl.backend.weddinggallery.user.model.SystemRole;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.user.repository.UserRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "app.security.csrf.enabled=true")
@ActiveProfiles("test")
class EventFlowIntegrationTest {
	@LocalServerPort
	private int port;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private EventMembershipRepository membershipRepository;
	@Autowired
	private AuditEventRepository auditEventRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		membershipRepository.deleteAll();
		eventRepository.deleteAll();
		auditEventRepository.deleteAll();
		userRepository.deleteAll();
		createUser("owner@example.com");
		createUser("manager@example.com");
		createUser("outsider@example.com");
	}

	@Test
	void shouldEnforceOwnershipMembershipIdorCsrfAndTransferAcrossRealSessions() throws Exception {
		SessionClient owner = login("owner@example.com");
		SessionClient manager = login("manager@example.com");
		SessionClient outsider = login("outsider@example.com");

		ResponseEntity<String> create = owner.exchange(HttpMethod.POST, "/api/events", eventPayload("Stage 3"), true);
		assertThat(create.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		String eventId = jsonValue(create, "id");

		ResponseEntity<String> noCsrf = owner.exchange(HttpMethod.POST, "/api/events", eventPayload("Blocked"), false);
		assertThat(noCsrf.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(eventRepository.count()).isEqualTo(1);

		ResponseEntity<String> foreign = outsider.exchange(HttpMethod.GET, "/api/events/" + eventId, null, false);
		ResponseEntity<String> missing = outsider.exchange(HttpMethod.GET,
				"/api/events/00000000-0000-0000-0000-000000000099", null, false);
		assertThat(foreign.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(jsonValue(foreign, "code")).isEqualTo("EVENT_NOT_FOUND");

		Map<String, String> addManager = Map.of("email", "manager@example.com", "role", "MANAGER");
		ResponseEntity<String> added = owner.exchange(HttpMethod.POST, "/api/events/" + eventId + "/members",
				addManager, true);
		assertThat(added.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		String membershipId = jsonValue(added, "id");
		assertThat(owner.exchange(HttpMethod.POST, "/api/events/" + eventId + "/members", addManager, true)
				.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

		assertThat(manager.exchange(HttpMethod.GET, "/api/events/" + eventId, null, false).getStatusCode())
				.isEqualTo(HttpStatus.OK);
		assertThat(manager.exchange(HttpMethod.PUT, "/api/events/" + eventId, eventPayload("Manager edit"), true)
				.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(manager.exchange(HttpMethod.DELETE, "/api/events/" + eventId, null, true).getStatusCode())
				.isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(manager.exchange(HttpMethod.POST, "/api/events/" + eventId + "/members",
				Map.of("email", "outsider@example.com", "role", "MANAGER"), true).getStatusCode())
				.isEqualTo(HttpStatus.FORBIDDEN);

		ResponseEntity<String> secondCreate = owner.exchange(HttpMethod.POST, "/api/events", eventPayload("Other"),
				true);
		String secondEventId = jsonValue(secondCreate, "id");
		assertThat(owner
				.exchange(HttpMethod.DELETE, "/api/events/" + secondEventId + "/members/" + membershipId, null, true)
				.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		assertThat(owner.exchange(HttpMethod.DELETE, "/api/events/" + eventId + "/members/" + membershipId, null, true)
				.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(manager.exchange(HttpMethod.GET, "/api/events/" + eventId, null, false).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);

		ResponseEntity<String> readded = owner.exchange(HttpMethod.POST, "/api/events/" + eventId + "/members",
				addManager, true);
		assertThat(readded.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(jsonValue(readded, "id")).isEqualTo(membershipId);

		ResponseEntity<String> transferred = owner.exchange(HttpMethod.POST,
				"/api/events/" + eventId + "/ownership-transfer", Map.of("targetMembershipId", membershipId), true);
		assertThat(transferred.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(jsonValue(transferred, "currentUserRole")).isEqualTo("MANAGER");
		assertThat(owner.exchange(HttpMethod.DELETE, "/api/events/" + eventId, null, true).getStatusCode())
				.isEqualTo(HttpStatus.FORBIDDEN);

		ResponseEntity<String> archived = manager.exchange(HttpMethod.POST, "/api/events/" + eventId + "/archive", null,
				true);
		assertThat(archived.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(jsonValue(archived, "status")).isEqualTo("ARCHIVED");
		assertThat(manager.exchange(HttpMethod.PUT, "/api/events/" + eventId, eventPayload("Too late"), true)
				.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

		assertThat(auditEventRepository.findAll()).extracting("eventType").contains(EventType.EVENT_CREATED,
				EventType.EVENT_UPDATED, EventType.EVENT_MANAGER_ADDED, EventType.EVENT_MANAGER_REMOVED,
				EventType.EVENT_OWNERSHIP_TRANSFERRED, EventType.EVENT_ARCHIVED);
	}

	private Map<String, Object> eventPayload(String name) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("name", name);
		payload.put("type", "WEDDING");
		payload.put("eventDate", "2026-08-15");
		payload.put("description", "Integration flow");
		payload.put("privacyMode", "PRIVATE");
		return payload;
	}

	private String jsonValue(ResponseEntity<String> response, String field) {
		Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
				.matcher(response.getBody() == null ? "" : response.getBody());
		assertThat(matcher.find()).as("response field %s in %s", field, response.getBody()).isTrue();
		return matcher.group(1);
	}

	private void createUser(String email) {
		userRepository.save(User.builder().email(email).passwordHash(passwordEncoder.encode("password123"))
				.systemRole(SystemRole.USER).failedLoginAttempts(0).lockedUntil(null).createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).build());
	}

	private SessionClient login(String email) {
		SessionClient client = new SessionClient();
		client.exchange(HttpMethod.GET, "/api/auth/csrf", null, false);
		ResponseEntity<String> response = client.exchange(HttpMethod.POST, "/api/auth/login",
				Map.of("email", email, "password", "password123"), true);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		return client;
	}

	private final class SessionClient {
		private final RestTemplate restTemplate;
		private final Map<String, String> cookies = new LinkedHashMap<>();

		private SessionClient() {
			restTemplate = new RestTemplate();
			restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
				@Override
				public boolean hasError(ClientHttpResponse response) {
					return false;
				}
			});
		}

		private ResponseEntity<String> exchange(HttpMethod method, String path, Object body, boolean includeCsrf) {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			if (!cookies.isEmpty()) {
				headers.set(HttpHeaders.COOKIE,
						cookies.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
								.reduce((a, b) -> a + "; " + b).orElse(""));
			}
			if (includeCsrf && cookies.containsKey("XSRF-TOKEN")) {
				headers.set("X-XSRF-TOKEN", cookies.get("XSRF-TOKEN"));
			}
			ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port + path, method,
					new HttpEntity<>(body, headers), String.class);
			List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
			if (setCookies != null) {
				for (String setCookie : setCookies) {
					String pair = setCookie.split(";", 2)[0];
					String[] parts = pair.split("=", 2);
					cookies.put(parts[0], parts.length > 1 ? parts[1] : "");
				}
			}
			return response;
		}
	}
}
