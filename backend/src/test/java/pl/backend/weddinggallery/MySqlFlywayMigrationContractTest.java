package pl.backend.weddinggallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class MySqlFlywayMigrationContractTest {
	@Container
	private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
			.withDatabaseName("wedding_gallery_contract").withUsername("wedding").withPassword("wedding");

	@Test
	void allMigrationsAndCriticalConstraintsWorkOnProductionDatabaseEngine() throws Exception {
		Flyway flyway = Flyway.configure().dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
				.locations("classpath:db/migration").load();
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

		assertThat(flyway.migrate().migrationsExecuted).isEqualTo(12);

		try (Connection connection = MYSQL.createConnection("");
				var users = connection.prepareStatement("SELECT COUNT(*) FROM users");
				var demoUsers = connection.prepareStatement(
						"SELECT COUNT(*) FROM users " + "WHERE email IN ('admin@example.com', 'owner@example.com', "
								+ "'manager@example.com', 'guest-tester@example.com')");
				var admin = connection.prepareStatement(
						"SELECT system_role, password_hash FROM users WHERE email = 'admin@example.com'");
				var managerMembership = connection.prepareStatement("SELECT COUNT(*) FROM event_memberships "
						+ "WHERE event_id = '00000000-0000-0000-0000-000000000711' "
						+ "AND user_id = (SELECT id FROM users WHERE email = 'manager@example.com') "
						+ "AND role = 'MANAGER' AND removed_at IS NULL");
				var demoGallery = connection.prepareStatement("SELECT public_view_enabled, upload_enabled, "
						+ "download_enabled, moderation_mode, access_code_hash FROM galleries "
						+ "WHERE slug = 'guest-uploads-demo-2945795a'");
				var demoAccess = connection.prepareStatement("SELECT COUNT(*) FROM gallery_accesses "
						+ "WHERE token_hash = '7203fdb1bf057575dd9c46070322fa9819d70219e686d399eb822e3cb171e28e' "
						+ "AND revoked_at IS NULL");
				var tables = connection.prepareStatement("SELECT table_name FROM information_schema.tables "
						+ "WHERE table_schema = DATABASE() AND table_name IN "
						+ "('galleries', 'gallery_accesses', 'upload_sessions', 'media_files', "
						+ "'media_processing_jobs', 'media_thumbnails')");
				var uniqueGrantKey = connection.prepareStatement("SELECT COUNT(*) FROM information_schema.statistics "
						+ "WHERE table_schema = DATABASE() AND table_name = 'upload_sessions' "
						+ "AND index_name = 'uk_upload_grant_idempotency'");
				var uniqueProcessingJobType = connection
						.prepareStatement("SELECT COUNT(*) FROM information_schema.statistics "
								+ "WHERE table_schema = DATABASE() AND table_name = 'media_processing_jobs' "
								+ "AND index_name = 'uk_media_processing_job_type'");
				var uniqueThumbnailVariant = connection
						.prepareStatement("SELECT COUNT(*) FROM information_schema.statistics "
								+ "WHERE table_schema = DATABASE() AND table_name = 'media_thumbnails' "
								+ "AND index_name = 'uk_media_thumbnail_variant'");
				var foreignKeys = connection
						.prepareStatement("SELECT COUNT(*) FROM information_schema.referential_constraints "
								+ "WHERE constraint_schema = DATABASE() AND table_name IN "
								+ "('gallery_accesses', 'upload_sessions', 'media_files', "
								+ "'media_processing_jobs', 'media_thumbnails')")) {
			assertThat(singleCount(users.executeQuery())).isEqualTo(4);
			assertThat(singleCount(demoUsers.executeQuery())).isEqualTo(4);
			ResultSet adminRow = admin.executeQuery();
			assertThat(adminRow.next()).isTrue();
			assertThat(adminRow.getString("system_role")).isEqualTo("ADMIN");
			assertThat(adminRow.getString("password_hash")).isNotEqualTo("password123!");
			assertThat(passwordEncoder.matches("password123!", adminRow.getString("password_hash"))).isTrue();
			assertThat(adminRow.next()).isFalse();
			assertThat(singleCount(managerMembership.executeQuery())).isEqualTo(1);
			ResultSet galleryRow = demoGallery.executeQuery();
			assertThat(galleryRow.next()).isTrue();
			assertThat(galleryRow.getBoolean("public_view_enabled")).isTrue();
			assertThat(galleryRow.getBoolean("upload_enabled")).isTrue();
			assertThat(galleryRow.getBoolean("download_enabled")).isFalse();
			assertThat(galleryRow.getString("moderation_mode")).isEqualTo("REQUIRED");
			assertThat(galleryRow.getString("access_code_hash")).isNotEqualTo("password123!");
			assertThat(passwordEncoder.matches("password123!", galleryRow.getString("access_code_hash"))).isTrue();
			assertThat(galleryRow.next()).isFalse();
			assertThat(singleCount(demoAccess.executeQuery())).isEqualTo(1);
			ResultSet tableRows = tables.executeQuery();
			int tableCount = 0;
			while (tableRows.next())
				tableCount++;
			assertThat(tableCount).isEqualTo(6);
			assertThat(singleCount(uniqueGrantKey.executeQuery())).isPositive();
			assertThat(singleCount(uniqueProcessingJobType.executeQuery())).isPositive();
			assertThat(singleCount(uniqueThumbnailVariant.executeQuery())).isPositive();
			assertThat(singleCount(foreignKeys.executeQuery())).isEqualTo(7);
		}
	}

	private int singleCount(ResultSet result) throws Exception {
		assertThat(result.next()).isTrue();
		return result.getInt(1);
	}
}
