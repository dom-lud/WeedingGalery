package pl.backend.weddinggallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class FlywayMigrationContractTest {
	@Test
	void baselineV1CreatesTheCompleteImplementedSchemaOnDevelopmentH2InMySqlMode() throws Exception {
		String url = "jdbc:h2:mem:flyway-contract;DB_CLOSE_DELAY=-1;MODE=MySQL";
		Flyway flyway = Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").load();
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

		assertThat(flyway.migrate().migrationsExecuted).isEqualTo(11);

		try (var connection = DriverManager.getConnection(url, "sa", "");
				var users = connection.prepareStatement("SELECT COUNT(*) FROM users");
				var demoUsers = connection.prepareStatement("SELECT email, system_role, password_hash FROM users "
						+ "WHERE email IN ('admin@example.com', 'owner@example.com', "
						+ "'manager@example.com', 'guest-tester@example.com') ORDER BY email");
				var events = connection.prepareStatement("SELECT owner_user_id, privacy_mode, version FROM events");
				var membership = connection.prepareStatement("SELECT removed_at, version FROM event_memberships");
				var audit = connection.prepareStatement("SELECT event_id, target_user_id FROM audit_events");
				var galleries = connection.prepareStatement(
						"SELECT event_id, slug, name, status, sort_order, archived_at, deleted_at, version FROM galleries");
				var galleryAudit = connection.prepareStatement("SELECT gallery_id FROM audit_events");
				var gallerySettings = connection.prepareStatement("SELECT public_view_enabled, upload_enabled, "
						+ "moderation_mode, storage_used_bytes, storage_reserved_bytes FROM galleries");
				var galleryAccess = connection
						.prepareStatement("SELECT gallery_id, token_hash, revoked_at FROM gallery_accesses");
				var uploadSessions = connection
						.prepareStatement("SELECT gallery_id, public_access_id, grant_fingerprint, "
								+ "idempotency_key, request_fingerprint, status, reserved_bytes FROM upload_sessions");
				var mediaFiles = connection.prepareStatement("SELECT upload_session_id, gallery_id, storage_key, "
						+ "declared_content_type, detected_content_type, status, checksum_sha256, "
						+ "width, height, processed_at FROM media_files");
				var mediaProcessingJobs = connection.prepareStatement("SELECT media_file_id, job_type, status, "
						+ "attempt_count, max_attempts, locked_by FROM media_processing_jobs");
				var mediaThumbnails = connection.prepareStatement("SELECT media_file_id, variant, storage_key, "
						+ "width, height, size_bytes FROM media_thumbnails");
				var guestAudit = connection
						.prepareStatement("SELECT actor_type, public_access_id, user_email FROM audit_events");
				var demoGallery = connection.prepareStatement("SELECT public_view_enabled, upload_enabled, "
						+ "download_enabled, moderation_mode, access_code_hash FROM galleries "
						+ "WHERE slug = 'guest-uploads-demo-2945795a'");
				var demoAccess = connection.prepareStatement("SELECT token_hash, revoked_at FROM gallery_accesses "
						+ "WHERE token_hash = '7203fdb1bf057575dd9c46070322fa9819d70219e686d399eb822e3cb171e28e'");
				var history = connection.prepareStatement("SELECT \"version\" FROM \"flyway_schema_history\" "
						+ "WHERE \"success\" = TRUE AND \"version\" IS NOT NULL ORDER BY \"installed_rank\"")) {
			assertThat(users.executeQuery()).satisfies(result -> {
				assertThat(result.next()).isTrue();
				assertThat(result.getInt(1)).isEqualTo(4);
			});
			assertThat(demoUsers.executeQuery()).satisfies(result -> {
				assertThat(result.next()).isTrue();
				assertThat(result.getString("email")).isEqualTo("admin@example.com");
				assertThat(result.getString("system_role")).isEqualTo("ADMIN");
				assertThat(result.getString("password_hash")).isNotEqualTo("password123!");
				assertThat(passwordEncoder.matches("password123!", result.getString("password_hash"))).isTrue();
				assertThat(result.next()).isTrue();
				assertThat(result.getString("email")).isEqualTo("guest-tester@example.com");
				assertThat(result.getString("system_role")).isEqualTo("USER");
				assertThat(passwordEncoder.matches("password123!", result.getString("password_hash"))).isTrue();
				assertThat(result.next()).isTrue();
				assertThat(result.getString("email")).isEqualTo("manager@example.com");
				assertThat(result.getString("system_role")).isEqualTo("USER");
				assertThat(passwordEncoder.matches("password123!", result.getString("password_hash"))).isTrue();
				assertThat(result.next()).isTrue();
				assertThat(result.getString("email")).isEqualTo("owner@example.com");
				assertThat(result.getString("system_role")).isEqualTo("USER");
				assertThat(passwordEncoder.matches("password123!", result.getString("password_hash"))).isTrue();
				assertThat(result.next()).isFalse();
			});
			assertThat(events.executeQuery()).isNotNull();
			assertThat(membership.executeQuery()).isNotNull();
			assertThat(audit.executeQuery()).isNotNull();
			assertThat(galleries.executeQuery()).isNotNull();
			assertThat(galleryAudit.executeQuery()).isNotNull();
			assertThat(gallerySettings.executeQuery()).isNotNull();
			assertThat(galleryAccess.executeQuery()).isNotNull();
			assertThat(uploadSessions.executeQuery()).isNotNull();
			assertThat(mediaFiles.executeQuery()).isNotNull();
			assertThat(mediaProcessingJobs.executeQuery()).isNotNull();
			assertThat(mediaThumbnails.executeQuery()).isNotNull();
			assertThat(guestAudit.executeQuery()).isNotNull();
			assertThat(demoGallery.executeQuery()).satisfies(result -> {
				assertThat(result.next()).isTrue();
				assertThat(result.getBoolean("public_view_enabled")).isTrue();
				assertThat(result.getBoolean("upload_enabled")).isTrue();
				assertThat(result.getBoolean("download_enabled")).isFalse();
				assertThat(result.getString("moderation_mode")).isEqualTo("REQUIRED");
				assertThat(result.getString("access_code_hash")).isNotEqualTo("password123!");
				assertThat(passwordEncoder.matches("password123!", result.getString("access_code_hash"))).isTrue();
				assertThat(result.next()).isFalse();
			});
			assertThat(demoAccess.executeQuery()).satisfies(result -> {
				assertThat(result.next()).isTrue();
				assertThat(result.getString("revoked_at")).isNull();
				assertThat(result.next()).isFalse();
			});
			var versions = history.executeQuery();
			assertThat(versions.next()).isTrue();
			assertThat(versions.getString(1)).isEqualTo("1");
			assertThat(versions.next()).isTrue();
			assertThat(versions.getString(1)).isEqualTo("2");
			assertThat(versions.next()).isTrue();
			assertThat(versions.getString(1)).isEqualTo("3");
			assertThat(versions.next()).isTrue();
			assertThat(versions.getString(1)).isEqualTo("4");
			assertThat(versions.next()).isTrue();
			assertThat(versions.getString(1)).isEqualTo("5");
			assertThat(versions.next()).isTrue();
			assertThat(versions.getString(1)).isEqualTo("6");
			assertThat(versions.next()).isTrue();
			assertThat(versions.getString(1)).isEqualTo("7");
			assertThat(versions.next()).isTrue();
			assertThat(versions.getString(1)).isEqualTo("8");
			assertThat(versions.next()).isTrue();
			assertThat(versions.getString(1)).isEqualTo("9");
			assertThat(versions.next()).isTrue();
			assertThat(versions.getString(1)).isEqualTo("10");
			assertThat(versions.next()).isTrue();
			assertThat(versions.getString(1)).isEqualTo("11");
			assertThat(versions.next()).isFalse();
		}
	}
}
