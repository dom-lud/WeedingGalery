package pl.backend.weddinggallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class FlywayMigrationContractTest {
	@Test
	void baselineV1CreatesTheCompleteImplementedSchemaOnDevelopmentH2InMySqlMode() throws Exception {
		String url = "jdbc:h2:mem:flyway-contract;DB_CLOSE_DELAY=-1;MODE=MySQL";
		Flyway flyway = Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").load();

		assertThat(flyway.migrate().migrationsExecuted).isEqualTo(5);

		try (var connection = DriverManager.getConnection(url, "sa", "");
				var users = connection.prepareStatement("SELECT COUNT(*) FROM users");
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
						+ "declared_content_type, detected_content_type, status, checksum_sha256 FROM media_files");
				var guestAudit = connection
						.prepareStatement("SELECT actor_type, public_access_id, user_email FROM audit_events");
				var history = connection.prepareStatement("SELECT \"version\" FROM \"flyway_schema_history\" "
						+ "WHERE \"success\" = TRUE AND \"version\" IS NOT NULL ORDER BY \"installed_rank\"")) {
			assertThat(users.executeQuery()).satisfies(result -> {
				assertThat(result.next()).isTrue();
				assertThat(result.getInt(1)).isZero();
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
			assertThat(guestAudit.executeQuery()).isNotNull();
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
			assertThat(versions.next()).isFalse();
		}
	}
}
