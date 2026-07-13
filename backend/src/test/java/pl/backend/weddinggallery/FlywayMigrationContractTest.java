package pl.backend.weddinggallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class FlywayMigrationContractTest {
	@Test
	void allMigrationsRunOnDevelopmentH2InMySqlMode() throws Exception {
		String url = "jdbc:h2:mem:flyway-contract;DB_CLOSE_DELAY=-1;MODE=MySQL";
		Flyway flyway = Flyway.configure().dataSource(url, "sa", "")
				.locations("classpath:db/migration/common", "classpath:db/migration/h2", "classpath:db/seed").load();

		assertThat(flyway.migrate().migrationsExecuted).isEqualTo(4);

		try (var connection = DriverManager.getConnection(url, "sa", "");
				var membership = connection.prepareStatement("SELECT COUNT(*) FROM event_memberships");
				var history = connection
						.prepareStatement("SELECT \"version\" FROM \"flyway_schema_history\" WHERE \"success\" = TRUE")) {
			assertThat(membership.executeQuery().next()).isTrue();
			var versions = history.executeQuery();
			assertThat(versions.next()).isTrue();
		}
	}
}
