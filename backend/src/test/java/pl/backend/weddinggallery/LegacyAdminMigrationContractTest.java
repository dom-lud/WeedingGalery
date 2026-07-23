package pl.backend.weddinggallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.time.LocalDateTime;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class LegacyAdminMigrationContractTest {

	@Test
	void v5AndLaterPreserveReferencedLegacyAdministratorButDisableTheKnownCredential() throws Exception {
		String url = "jdbc:h2:mem:legacy-admin-contract;DB_CLOSE_DELAY=-1;MODE=MySQL";
		Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").target("4").load().migrate();

		try (var connection = DriverManager.getConnection(url, "sa", "");
				var rename = connection.prepareStatement("UPDATE users SET email = 'renamed-admin@example.com' "
						+ "WHERE id = '00000000-0000-0000-0000-000000000001'");
				var insert = connection.prepareStatement("INSERT INTO events "
						+ "(id, name, type, status, owner_user_id, privacy_mode, created_at, updated_at, version) "
						+ "VALUES ('legacy-event', 'Legacy event', 'WEDDING', 'ACTIVE', "
						+ "'00000000-0000-0000-0000-000000000001', 'PRIVATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)")) {
			rename.executeUpdate();
			insert.executeUpdate();
		}

		Flyway flyway = Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").load();
		assertThat(flyway.migrate().migrationsExecuted).isGreaterThanOrEqualTo(2);

		try (var connection = DriverManager.getConnection(url, "sa", "");
				var statement = connection.prepareStatement("SELECT password_hash, locked_until FROM users WHERE id = "
						+ "'00000000-0000-0000-0000-000000000001'");
				var result = statement.executeQuery()) {
			assertThat(result.next()).isTrue();
			assertThat(result.getString("password_hash")).isEqualTo("BOOTSTRAP_DISABLED");
			assertThat(result.getTimestamp("locked_until").toLocalDateTime())
					.isEqualTo(LocalDateTime.of(2038, 1, 18, 23, 59, 59));
			assertThat(result.next()).isFalse();
		}
	}
}
