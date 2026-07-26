package pl.backend.weddinggallery.upload.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.*;
import java.util.regex.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.repository.AuditEventRepository;
import pl.backend.weddinggallery.event.model.*;
import pl.backend.weddinggallery.event.repository.EventRepository;
import pl.backend.weddinggallery.gallery.model.*;
import pl.backend.weddinggallery.gallery.repository.GalleryRepository;
import pl.backend.weddinggallery.media.repository.MediaFileRepository;
import pl.backend.weddinggallery.media.repository.MediaProcessingJobRepository;
import pl.backend.weddinggallery.membership.repository.EventMembershipRepository;
import pl.backend.weddinggallery.publicaccess.repository.GalleryAccessRepository;
import pl.backend.weddinggallery.storage.StorageService;
import pl.backend.weddinggallery.upload.repository.UploadSessionRepository;
import pl.backend.weddinggallery.upload.service.UploadService;
import pl.backend.weddinggallery.user.model.*;
import pl.backend.weddinggallery.user.repository.UserRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {"app.security.csrf.enabled=true",
		"app.public-access.rate-limit=1000", "app.storage.local.root=./target/test-media-public"})
@ActiveProfiles("test")
class PublicGalleryUploadFlowIntegrationTest {
	@LocalServerPort
	private int port;
	@Autowired
	private UserRepository users;
	@Autowired
	private EventRepository events;
	@Autowired
	private EventMembershipRepository memberships;
	@Autowired
	private GalleryRepository galleries;
	@Autowired
	private GalleryAccessRepository accesses;
	@Autowired
	private UploadSessionRepository sessions;
	@Autowired
	private MediaFileRepository media;
	@Autowired
	private MediaProcessingJobRepository processingJobs;
	@Autowired
	private AuditEventRepository audit;
	@Autowired
	private PasswordEncoder passwords;
	@Autowired
	private StorageService storage;
	@Autowired
	private UploadService uploadService;
	private String eventId;
	private String galleryId;
	private String slug;

	@BeforeEach
	void setUp() {
		media.findAll().forEach(file -> {
			if (storage.exists(file.getStorageKey()))
				storage.delete(file.getStorageKey());
		});
		processingJobs.deleteAll();
		media.deleteAll();
		sessions.deleteAll();
		accesses.deleteAll();
		galleries.deleteAll();
		memberships.deleteAll();
		events.deleteAll();
		audit.deleteAll();
		users.deleteAll();
		User owner = users.save(User.builder().email("owner@example.com").passwordHash(passwords.encode("password123"))
				.systemRole(SystemRole.USER).failedLoginAttempts(0).createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).build());
		Event event = events
				.save(Event.builder().name("Public event").type(pl.backend.weddinggallery.event.model.EventType.WEDDING)
						.eventDate(LocalDate.of(2026, 8, 15)).description("Public flow")
						.privacyMode(PrivacyMode.PRIVATE).owner(owner).status(EventStatus.DRAFT).build());
		Gallery gallery = galleries.save(Gallery.builder().event(event).name("Guest photos").slug("guest-photos-secure")
				.description("Upload here").sortOrder(0).status(GalleryStatus.ACTIVE).build());
		eventId = event.getId();
		galleryId = gallery.getId();
		slug = gallery.getSlug();
	}

	@Test
	void enforcesPublicAccessAndPartialUploadContractEndToEnd() {
		SessionClient owner = login("owner@example.com");
		String management = "/api/events/" + eventId + "/galleries/" + galleryId;
		assertThat(
				owner.json(HttpMethod.POST, management + "/access-token/rotate", null, false, Map.of()).getStatusCode())
				.isEqualTo(HttpStatus.FORBIDDEN);
		ResponseEntity<String> rotated = owner.json(HttpMethod.POST, management + "/access-token/rotate", null, true,
				Map.of());
		assertThat(rotated.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		String token = string(rotated, "accessToken");
		assertThat(rotated.getBody()).contains("#token=").doesNotContain("tokenHash");

		assertThat(owner
				.json(HttpMethod.PUT, management + "/access-code", Map.of("accessCode", "Secret42"), true, Map.of())
				.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		ResponseEntity<String> settings = owner.json(HttpMethod.GET, management + "/settings", null, false, Map.of());
		long version = number(settings, "version");
		Map<String, Object> settingsBody = new LinkedHashMap<>();
		settingsBody.put("publicViewEnabled", true);
		settingsBody.put("uploadEnabled", true);
		settingsBody.put("downloadEnabled", true);
		settingsBody.put("moderationMode", "NONE");
		settingsBody.put("publishedAt", null);
		settingsBody.put("expiresAt", null);
		settingsBody.put("version", version);
		ResponseEntity<String> updatedSettings = owner.json(HttpMethod.PUT, management + "/settings", settingsBody,
				true, Map.of());
		assertThat(updatedSettings.getStatusCode()).isEqualTo(HttpStatus.OK);
		long updatedVersion = number(updatedSettings, "version");
		assertThat(updatedVersion).isGreaterThan(version);
		Map<String, Object> futureSettings = new LinkedHashMap<>(settingsBody);
		futureSettings.put("publishedAt", Instant.now().plusSeconds(3600).toString());
		futureSettings.put("version", updatedVersion);
		ResponseEntity<String> futureUpdated = owner.json(HttpMethod.PUT, management + "/settings", futureSettings,
				true, Map.of());
		SessionClient earlyGuest = new SessionClient();
		earlyGuest.json(HttpMethod.GET, "/api/auth/csrf", null, false, Map.of());
		assertThat(earlyGuest.json(HttpMethod.POST, "/api/public/galleries/" + slug + "/access",
				Map.of("accessToken", token, "accessCode", "Secret42"), true, Map.of()).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
		settingsBody.put("version", number(futureUpdated, "version"));
		updatedSettings = owner.json(HttpMethod.PUT, management + "/settings", settingsBody, true, Map.of());
		updatedVersion = number(updatedSettings, "version");

		SessionClient guest = new SessionClient();
		guest.json(HttpMethod.GET, "/api/auth/csrf", null, false, Map.of());
		String accessPath = "/api/public/galleries/" + slug + "/access";
		assertThat(guest.json(HttpMethod.POST, accessPath, Map.of("accessToken", token, "accessCode", "Secret42"),
				false, Map.of()).getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
		assertThat(guest.json(HttpMethod.POST, accessPath,
				Map.of("accessToken", "wrong-token", "accessCode", "Secret42"), true, Map.of()).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(
				guest.json(HttpMethod.POST, accessPath, Map.of("accessToken", token), true, Map.of()).getStatusCode())
				.isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(guest.json(HttpMethod.POST, accessPath, Map.of("accessToken", token, "accessCode", "Secret42"), true,
				Map.of()).getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(guest.json(HttpMethod.GET, "/api/public/galleries/" + slug, null, false, Map.of()).getStatusCode())
				.isEqualTo(HttpStatus.OK);

		byte[] jpeg = jpeg();
		List<Map<String, Object>> files = List.of(file("good", "good.jpg", "image/jpeg", jpeg.length),
				file("spoofed", "spoofed.jpg", "image/jpeg", 4));
		Map<String, Object> manifest = Map.of("files", files);
		String sessionsPath = "/api/public/galleries/" + slug + "/upload-sessions";
		assertThat(guest.multipart(sessionsPath + "/missing-session/files/good", "good.jpg", "image/jpeg", jpeg, true)
				.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(guest.json(HttpMethod.POST, sessionsPath, manifest, false, Map.of("Idempotency-Key", "batch-0001"))
				.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
		ResponseEntity<String> created = guest.json(HttpMethod.POST, sessionsPath, manifest, true,
				Map.of("Idempotency-Key", "batch-0001"));
		assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		String sessionId = string(created, "id");
		SessionClient secondGuest = new SessionClient();
		secondGuest.json(HttpMethod.GET, "/api/auth/csrf", null, false, Map.of());
		assertThat(secondGuest.json(HttpMethod.POST, accessPath, Map.of("accessToken", token, "accessCode", "Secret42"),
				true, Map.of()).getStatusCode()).isEqualTo(HttpStatus.OK);
		ResponseEntity<String> secondCreated = secondGuest.json(HttpMethod.POST, sessionsPath, manifest, true,
				Map.of("Idempotency-Key", "batch-0001"));
		assertThat(secondCreated.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		String secondSessionId = string(secondCreated, "id");
		assertThat(secondSessionId).isNotEqualTo(sessionId);
		var expiring = sessions.findById(secondSessionId).orElseThrow();
		expiring.setExpiresAt(LocalDateTime.now().minusSeconds(1));
		sessions.saveAndFlush(expiring);
		uploadService.releaseExpiredAndRevokedSessions();
		assertThat(sessions.findById(secondSessionId).orElseThrow().getStatus().name()).isEqualTo("EXPIRED");
		assertThat(guest.json(HttpMethod.POST, sessionsPath, manifest, true, Map.of("Idempotency-Key", "batch-0001"))
				.getStatusCode()).isEqualTo(HttpStatus.OK);
		Map<String, Object> changed = Map.of("files", List.of(file("good", "changed.jpg", "image/jpeg", 4)));
		assertThat(guest.json(HttpMethod.POST, sessionsPath, changed, true, Map.of("Idempotency-Key", "batch-0001"))
				.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

		assertThat(guest.multipart(sessionsPath + "/" + sessionId + "/files/good", "good.jpg", "image/jpeg", jpeg, true)
				.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(processingJobs.count()).isEqualTo(1);
		var uploadedMedia = media.findByUploadSessionIdAndClientFileId(sessionId, "good").orElseThrow();
		assertThat(uploadedMedia.getStatus().name()).isEqualTo("PROCESSING");
		ResponseEntity<String> publicGalleryAfterUpload = guest.json(HttpMethod.GET, "/api/public/galleries/" + slug,
				null, false, Map.of());
		assertThat(publicGalleryAfterUpload.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(publicGalleryAfterUpload.getBody()).contains("\"media\"", "good.jpg", "thumbnailUrl", "contentUrl")
				.doesNotContain("storageKey", "checksumSha256");
		ResponseEntity<String> publicContent = guest.json(HttpMethod.GET,
				"/api/public/galleries/" + slug + "/media/" + uploadedMedia.getId() + "/content", null, false,
				Map.of());
		assertThat(publicContent.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(publicContent.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_JPEG);
		ResponseEntity<String> ownerMedia = owner.json(HttpMethod.GET, management + "/media", null, false, Map.of());
		assertThat(ownerMedia.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(ownerMedia.getBody()).contains("good.jpg", "thumbnailUrl", "contentUrl").doesNotContain("storageKey",
				"checksumSha256");
		ResponseEntity<String> ownerDownload = owner.json(HttpMethod.GET, management + "/download", null, false,
				Map.of());
		assertThat(ownerDownload.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(ownerDownload.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("application/zip"));
		assertThat(ownerDownload.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains(".zip");
		ResponseEntity<String> spoofed = guest.multipart(sessionsPath + "/" + sessionId + "/files/spoofed",
				"spoofed.jpg", "image/jpeg", "text".getBytes(), true);
		assertThat(spoofed.getStatusCode().value()).isEqualTo(422);
		assertThat(string(spoofed, "code")).isEqualTo("UPLOAD_CONTENT_MISMATCH");
		ResponseEntity<String> state = guest.json(HttpMethod.GET, sessionsPath + "/" + sessionId, null, false,
				Map.of());
		assertThat(state.getBody()).contains("PROCESSING", "FAILED").doesNotContain("storageKey");
		assertThat(guest.json(HttpMethod.POST, sessionsPath + "/" + sessionId + "/cancel", null, true, Map.of())
				.getStatusCode()).isEqualTo(HttpStatus.OK);

		Gallery persisted = galleries.findById(galleryId).orElseThrow();
		assertThat(persisted.getStorageUsedBytes()).isEqualTo(jpeg.length);
		assertThat(persisted.getStorageReservedBytes()).isZero();
		assertThat(accesses.findAll()).allSatisfy(access -> assertThat(access.getTokenHash()).doesNotContain(token));
		assertThat(persisted.getAccessCodeHash()).doesNotContain("Secret42");
		assertThat(audit.findAll()).anySatisfy(entry -> {
			assertThat(entry.getEventType()).isEqualTo(EventType.MEDIA_STORED);
			assertThat(entry.getActorType()).isEqualTo("GUEST");
			assertThat(entry.getUserEmail()).isNull();
		});

		owner.json(HttpMethod.POST, management + "/access-token/rotate", null, true, Map.of());
		assertThat(guest.json(HttpMethod.GET, "/api/public/galleries/" + slug, null, false, Map.of()).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(owner.json(HttpMethod.POST, management + "/archive", null, true, Map.of()).getStatusCode())
				.isEqualTo(HttpStatus.OK);
		settingsBody.put("version", updatedVersion);
		assertThat(owner.json(HttpMethod.PUT, management + "/settings", settingsBody, true, Map.of()).getStatusCode())
				.isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	void rejectsInvalidKeysManifestLimitsActiveSessionOverflowAndGalleryQuota() {
		SessionClient owner = login("owner@example.com");
		SessionClient guest = new SessionClient();
		enableUploadAndGrant(owner, guest);
		String sessionsPath = "/api/public/galleries/" + slug + "/upload-sessions";
		Map<String, Object> oneFile = Map.of("files", List.of(file("one", "one.jpg", "image/jpeg", 4)));

		ResponseEntity<String> invalidKey = guest.json(HttpMethod.POST, sessionsPath, oneFile, true,
				Map.of("Idempotency-Key", "short"));
		assertThat(invalidKey.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(string(invalidKey, "code")).isEqualTo("UPLOAD_INVALID_IDEMPOTENCY_KEY");

		List<Map<String, Object>> tooManyFiles = new ArrayList<>();
		for (int index = 0; index < 51; index++)
			tooManyFiles.add(file("file-" + index, "photo-" + index + ".jpg", "image/jpeg", 4));
		assertThat(guest.json(HttpMethod.POST, sessionsPath, Map.of("files", tooManyFiles), true,
				Map.of("Idempotency-Key", "too-many-files")).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		List<Map<String, Object>> oversizedSession = new ArrayList<>();
		for (int index = 0; index < 5; index++)
			oversizedSession.add(file("video-" + index, "video-" + index + ".mp4", "video/mp4", 500L * 1024 * 1024));
		ResponseEntity<String> oversized = guest.json(HttpMethod.POST, sessionsPath, Map.of("files", oversizedSession),
				true, Map.of("Idempotency-Key", "oversized-session"));
		assertThat(oversized.getStatusCode().value()).isEqualTo(413);
		assertThat(string(oversized, "code")).isEqualTo("UPLOAD_FILE_TOO_LARGE");

		for (int index = 0; index < 3; index++)
			assertThat(guest.json(HttpMethod.POST, sessionsPath, oneFile, true,
					Map.of("Idempotency-Key", "active-session-" + index)).getStatusCode())
					.isEqualTo(HttpStatus.CREATED);
		ResponseEntity<String> fourth = guest.json(HttpMethod.POST, sessionsPath, oneFile, true,
				Map.of("Idempotency-Key", "active-session-3"));
		assertThat(fourth.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(string(fourth, "code")).isEqualTo("UPLOAD_SESSION_LIMIT_EXCEEDED");

		Gallery quotaConsumed = galleries.findById(galleryId).orElseThrow();
		quotaConsumed.setStorageUsedBytes(5L * 1024 * 1024 * 1024);
		galleries.saveAndFlush(quotaConsumed);
		SessionClient anotherGuest = new SessionClient();
		enableUploadAndGrant(owner, anotherGuest);
		ResponseEntity<String> quota = anotherGuest.json(HttpMethod.POST, sessionsPath, oneFile, true,
				Map.of("Idempotency-Key", "quota-overflow"));
		assertThat(quota.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(string(quota, "code")).isEqualTo("STORAGE_QUOTA_EXCEEDED");
	}

	@Test
	void isolatesUploadSessionsAndKeepsReplayAndCancelIdempotent() {
		SessionClient owner = login("owner@example.com");
		SessionClient firstGuest = new SessionClient();
		String token = enableUploadAndGrant(owner, firstGuest);
		String sessionsPath = "/api/public/galleries/" + slug + "/upload-sessions";
		byte[] jpeg = jpeg();
		Map<String, Object> manifest = Map.of("files", List.of(file("photo", "photo.jpg", "image/jpeg", jpeg.length)));
		ResponseEntity<String> created = firstGuest.json(HttpMethod.POST, sessionsPath, manifest, true,
				Map.of("Idempotency-Key", "replay-session"));
		String sessionId = string(created, "id");

		SessionClient secondGuest = new SessionClient();
		secondGuest.json(HttpMethod.GET, "/api/auth/csrf", null, false, Map.of());
		secondGuest.json(HttpMethod.POST, "/api/public/galleries/" + slug + "/access", Map.of("accessToken", token),
				true, Map.of());
		assertThat(
				secondGuest.json(HttpMethod.GET, sessionsPath + "/" + sessionId, null, false, Map.of()).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);

		String uploadPath = sessionsPath + "/" + sessionId + "/files/photo";
		assertThat(firstGuest.multipart(uploadPath, "photo.jpg", "image/jpeg", jpeg, true).getStatusCode())
				.isEqualTo(HttpStatus.OK);
		long mediaCount = media.count();
		long usedBytes = galleries.findById(galleryId).orElseThrow().getStorageUsedBytes();
		assertThat(firstGuest.multipart(uploadPath, "photo.jpg", "image/jpeg", jpeg, true).getStatusCode())
				.isEqualTo(HttpStatus.OK);
		assertThat(media.count()).isEqualTo(mediaCount);
		assertThat(galleries.findById(galleryId).orElseThrow().getStorageUsedBytes()).isEqualTo(usedBytes);

		String cancelPath = sessionsPath + "/" + sessionId + "/cancel";
		assertThat(firstGuest.json(HttpMethod.POST, cancelPath, null, true, Map.of()).getStatusCode())
				.isEqualTo(HttpStatus.OK);
		assertThat(firstGuest.json(HttpMethod.POST, cancelPath, null, true, Map.of()).getStatusCode())
				.isEqualTo(HttpStatus.OK);
		assertThat(galleries.findById(galleryId).orElseThrow().getStorageUsedBytes()).isEqualTo(usedBytes);
	}

	private String enableUploadAndGrant(SessionClient owner, SessionClient guest) {
		String management = "/api/events/" + eventId + "/galleries/" + galleryId;
		ResponseEntity<String> rotated = owner.json(HttpMethod.POST, management + "/access-token/rotate", null, true,
				Map.of());
		String token = string(rotated, "accessToken");
		ResponseEntity<String> settings = owner.json(HttpMethod.GET, management + "/settings", null, false, Map.of());
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("publicViewEnabled", true);
		body.put("uploadEnabled", true);
		body.put("downloadEnabled", false);
		body.put("moderationMode", "REQUIRED");
		body.put("publishedAt", null);
		body.put("expiresAt", null);
		body.put("version", number(settings, "version"));
		assertThat(owner.json(HttpMethod.PUT, management + "/settings", body, true, Map.of()).getStatusCode())
				.isEqualTo(HttpStatus.OK);
		guest.json(HttpMethod.GET, "/api/auth/csrf", null, false, Map.of());
		assertThat(guest.json(HttpMethod.POST, "/api/public/galleries/" + slug + "/access",
				Map.of("accessToken", token), true, Map.of()).getStatusCode()).isEqualTo(HttpStatus.OK);
		return token;
	}

	private Map<String, Object> file(String id, String name, String type, long size) {
		return Map.of("clientFileId", id, "fileName", name, "declaredContentType", type, "size", size);
	}
	private byte[] jpeg() {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "jpg", output);
			return output.toByteArray();
		} catch (Exception ex) {
			throw new AssertionError(ex);
		}
	}
	private SessionClient login(String email) {
		SessionClient client = new SessionClient();
		client.json(HttpMethod.GET, "/api/auth/csrf", null, false, Map.of());
		assertThat(client.json(HttpMethod.POST, "/api/auth/login", Map.of("email", email, "password", "password123"),
				true, Map.of()).getStatusCode()).isEqualTo(HttpStatus.OK);
		return client;
	}
	private String string(ResponseEntity<String> response, String field) {
		Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
				.matcher(Objects.toString(response.getBody(), ""));
		assertThat(matcher.find()).as("field %s in %s", field, response.getBody()).isTrue();
		return matcher.group(1);
	}
	private long number(ResponseEntity<String> response, String field) {
		Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*(\\d+)")
				.matcher(Objects.toString(response.getBody(), ""));
		assertThat(matcher.find()).isTrue();
		return Long.parseLong(matcher.group(1));
	}

	private final class SessionClient {
		private final RestTemplate http = new RestTemplate();
		private final Map<String, String> cookies = new LinkedHashMap<>();
		SessionClient() {
			http.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
			http.setErrorHandler(new DefaultResponseErrorHandler() {
				@Override
				public boolean hasError(ClientHttpResponse response) {
					return false;
				}
			});
		}
		ResponseEntity<String> json(HttpMethod method, String path, Object body, boolean csrf,
				Map<String, String> extra) {
			HttpHeaders headers = headers(csrf);
			headers.setContentType(MediaType.APPLICATION_JSON);
			extra.forEach(headers::set);
			return capture(http.exchange("http://localhost:" + port + path, method, new HttpEntity<>(body, headers),
					String.class));
		}
		ResponseEntity<String> multipart(String path, String filename, String type, byte[] bytes, boolean csrf) {
			HttpHeaders partHeaders = new HttpHeaders();
			partHeaders.setContentType(MediaType.parseMediaType(type));
			ByteArrayResource resource = new ByteArrayResource(bytes) {
				@Override
				public String getFilename() {
					return filename;
				}
			};
			LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", new HttpEntity<>(resource, partHeaders));
			HttpHeaders headers = headers(csrf);
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			return capture(http.exchange("http://localhost:" + port + path, HttpMethod.PUT,
					new HttpEntity<>(body, headers), String.class));
		}
		private HttpHeaders headers(boolean csrf) {
			HttpHeaders headers = new HttpHeaders();
			if (!cookies.isEmpty())
				headers.set(HttpHeaders.COOKIE, cookies.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
						.reduce((a, b) -> a + "; " + b).orElse(""));
			if (csrf && cookies.containsKey("XSRF-TOKEN"))
				headers.set("X-XSRF-TOKEN", cookies.get("XSRF-TOKEN"));
			return headers;
		}
		private ResponseEntity<String> capture(ResponseEntity<String> response) {
			List<String> values = response.getHeaders().get(HttpHeaders.SET_COOKIE);
			if (values != null)
				for (String value : values) {
					String[] pair = value.split(";", 2)[0].split("=", 2);
					cookies.put(pair[0], pair.length > 1 ? pair[1] : "");
				}
			return response;
		}
	}
}
