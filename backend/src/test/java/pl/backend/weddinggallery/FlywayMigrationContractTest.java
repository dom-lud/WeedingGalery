package pl.backend.weddinggallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class FlywayMigrationContractTest {
	@Test
	void baselineV1CreatesTheCompleteImplementedSchemaOnDevelopmentH2InMySqlMode() throws Exception {
		String url = "jdbc:h2:mem:flyway-contract;DB_CLOSE_DELAY=-1;MODE=MySQL";
		Flyway flyway = Flyway.configure().dataSource(url, "sa", "")
				.locations("classpath:db/migration").load();

		assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);

		try (var connection = DriverManager.getConnection(url, "sa", "");
				var users = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE email = 'admin@example.com'");
				var events = connection.prepareStatement("SELECT owner_user_id, privacy_mode, version FROM events");
				var membership = connection.prepareStatement("SELECT removed_at, version FROM event_memberships");
				var audit = connection.prepareStatement("SELECT event_id, target_user_id FROM audit_events");
				var galleries = connection.prepareStatement(
						"SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'GALLERIES'");
				var history = connection
						.prepareStatement("SELECT \"version\" FROM \"flyway_schema_history\" WHERE \"success\" = TRUE")) {
			assertThat(users.executeQuery()).satisfies(result -> {
				assertThat(result.next()).isTrue();
				assertThat(result.getInt(1)).isEqualTo(1);
			});
			assertThat(events.executeQuery()).isNotNull();
			assertThat(membership.executeQuery()).isNotNull();
			assertThat(audit.executeQuery()).isNotNull();
			assertThat(galleries.executeQuery()).satisfies(result -> {
				assertThat(result.next()).isTrue();
				assertThat(result.getInt(1)).isZero();
			});
			var versions = history.executeQuery();
			assertThat(versions.next()).isTrue();
			assertThat(versions.getString(1)).isEqualTo("1");
			assertThat(versions.next()).isFalse();
		}
	}
}
