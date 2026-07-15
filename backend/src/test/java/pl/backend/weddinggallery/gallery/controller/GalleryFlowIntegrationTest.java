package pl.backend.weddinggallery.gallery.controller;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.repository.AuditEventRepository;
import pl.backend.weddinggallery.event.repository.EventRepository;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.media.repository.MediaFileRepository;
import pl.backend.weddinggallery.membership.repository.EventMembershipRepository;
import pl.backend.weddinggallery.publicaccess.repository.GalleryAccessRepository;
import pl.backend.weddinggallery.upload.repository.UploadSessionRepository;
import pl.backend.weddinggallery.user.model.SystemRole;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.user.repository.UserRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "app.security.csrf.enabled=true")
@ActiveProfiles("test")
class GalleryFlowIntegrationTest {
	@LocalServerPort
	private int port;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private EventMembershipRepository membershipRepository;
	@Autowired
	private GalleryRepository galleryRepository;
	@Autowired
	private GalleryAccessRepository galleryAccessRepository;
	@Autowired
	private UploadSessionRepository uploadSessionRepository;
	@Autowired
	private MediaFileRepository mediaFileRepository;
	@Autowired
	private AuditEventRepository auditEventRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		mediaFileRepository.deleteAll();
		uploadSessionRepository.deleteAll();
		galleryAccessRepository.deleteAll();
		galleryRepository.deleteAll();
		membershipRepository.deleteAll();
		eventRepository.deleteAll();
		auditEventRepository.deleteAll();
		userRepository.deleteAll();
		createUser("owner@example.com");
		createUser("manager@example.com");
		createUser("outsider@example.com");
	}

	@Test
	void shouldEnforceGalleryContractAcrossRealSessions() {
		SessionClient owner = login("owner@example.com");
		SessionClient manager = login("manager@example.com");
		SessionClient outsider = login("outsider@example.com");
		String eventId = jsonValue(owner.exchange(HttpMethod.POST, "/api/events", eventPayload("Gallery event"), true),
				"id");
		String membershipId = jsonValue(owner.exchange(HttpMethod.POST, "/api/events/" + eventId + "/members",
				Map.of("email", "manager@example.com", "role", "MANAGER"), true), "id");

		ResponseEntity<String> noCsrf = owner.exchange(HttpMethod.POST, galleries(eventId),
				galleryPayload("Blocked", 1), false);
		assertThat(noCsrf.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

		ResponseEntity<String> later = owner.exchange(HttpMethod.POST, galleries(eventId),
				galleryPayload("Reception", 20), true);
		ResponseEntity<String> earlier = manager.exchange(HttpMethod.POST, galleries(eventId),
				galleryPayload("Preparations", 10), true);
		assertThat(later.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(earlier.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		String laterId = jsonValue(later, "id");
		String earlierId = jsonValue(earlier, "id");
		assertThat(jsonValue(earlier, "slug")).startsWith("preparations-");
		assertThat(manager.exchange(HttpMethod.GET, galleries(eventId) + "/" + earlierId, null, false).getStatusCode())
				.isEqualTo(HttpStatus.OK);

		ResponseEntity<String> list = manager.exchange(HttpMethod.GET, galleries(eventId), null, false);
		assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(list.getBody().indexOf(earlierId)).isLessThan(list.getBody().indexOf(laterId));
		assertThat(list.getBody()).doesNotContain("allowUpload", "visibility");

		ResponseEntity<String> foreign = outsider.exchange(HttpMethod.GET, galleries(eventId) + "/" + earlierId, null,
				false);
		assertThat(foreign.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(jsonValue(foreign, "code")).isEqualTo("EVENT_NOT_FOUND");

		String otherEventId = jsonValue(
				owner.exchange(HttpMethod.POST, "/api/events", eventPayload("Other event"), true), "id");
		ResponseEntity<String> crossEvent = owner.exchange(HttpMethod.GET, galleries(otherEventId) + "/" + earlierId,
				null, false);
		assertThat(crossEvent.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(jsonValue(crossEvent, "code")).isEqualTo("GALLERY_NOT_FOUND");

		ResponseEntity<String> updated = manager.exchange(HttpMethod.PUT, galleries(eventId) + "/" + earlierId,
				galleryPayload("Morning", 5), true);
		assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(jsonValue(updated, "slug")).isEqualTo(jsonValue(earlier, "slug"));
		assertThat(manager.exchange(HttpMethod.POST, galleries(eventId) + "/" + earlierId + "/archive", null, true)
				.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

		ResponseEntity<String> archived = owner.exchange(HttpMethod.POST,
				galleries(eventId) + "/" + earlierId + "/archive", null, true);
		assertThat(archived.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(jsonValue(archived, "status")).isEqualTo("ARCHIVED");
		assertThat(manager
				.exchange(HttpMethod.PUT, galleries(eventId) + "/" + earlierId, galleryPayload("Too late", 0), true)
				.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

		assertThat(owner.exchange(HttpMethod.DELETE, galleries(eventId) + "/" + laterId, null, true).getStatusCode())
				.isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(owner.exchange(HttpMethod.GET, galleries(eventId) + "/" + laterId, null, false).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(galleryRepository.findById(laterId).orElseThrow().getDeletedAt()).isNotNull();
		assertThat(galleryRepository.existsBySlug(jsonValue(later, "slug"))).isTrue();

		assertThat(owner.exchange(HttpMethod.DELETE, "/api/events/" + eventId + "/members/" + membershipId, null, true)
				.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(manager.exchange(HttpMethod.GET, galleries(eventId), null, false).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);

		assertThat(auditEventRepository.findAll()).filteredOn(event -> event.getGalleryId() != null)
				.extracting("eventType").contains(EventType.GALLERY_CREATED, EventType.GALLERY_UPDATED,
						EventType.GALLERY_ARCHIVED, EventType.GALLERY_DELETED);
	}

	@Test
	void shouldRejectInvalidGalleryPayloadWithoutPersistingIt() {
		SessionClient owner = login("owner@example.com");
		String eventId = jsonValue(owner.exchange(HttpMethod.POST, "/api/events", eventPayload("Validation"), true),
				"id");
		ResponseEntity<String> response = owner.exchange(HttpMethod.POST, galleries(eventId),
				Map.of("name", "   ", "description", "ok", "sortOrder", 100001), true);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(jsonValue(response, "code")).isEqualTo("VALIDATION_ERROR");
		assertThat(galleryRepository.count()).isZero();
	}

	private String galleries(String eventId) {
		return "/api/events/" + eventId + "/galleries";
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

	private Map<String, Object> galleryPayload(String name, int sortOrder) {
		return Map.of("name", name, "description", "Gallery integration flow", "sortOrder", sortOrder);
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
					String[] parts = setCookie.split(";", 2)[0].split("=", 2);
					cookies.put(parts[0], parts.length > 1 ? parts[1] : "");
				}
			}
			return response;
		}
	}
}
