package pl.backend.weddinggallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
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

		assertThat(flyway.migrate().migrationsExecuted).isEqualTo(5);

		try (Connection connection = MYSQL.createConnection("");
				var users = connection.prepareStatement("SELECT COUNT(*) FROM users");
				var tables = connection.prepareStatement("SELECT table_name FROM information_schema.tables "
						+ "WHERE table_schema = DATABASE() AND table_name IN "
						+ "('galleries', 'gallery_accesses', 'upload_sessions', 'media_files')");
				var uniqueGrantKey = connection.prepareStatement("SELECT COUNT(*) FROM information_schema.statistics "
						+ "WHERE table_schema = DATABASE() AND table_name = 'upload_sessions' "
						+ "AND index_name = 'uk_upload_grant_idempotency'");
				var foreignKeys = connection
						.prepareStatement("SELECT COUNT(*) FROM information_schema.referential_constraints "
								+ "WHERE constraint_schema = DATABASE() AND table_name IN ('gallery_accesses', 'upload_sessions', 'media_files')")) {
			assertThat(singleCount(users.executeQuery())).isZero();
			ResultSet tableRows = tables.executeQuery();
			int tableCount = 0;
			while (tableRows.next())
				tableCount++;
			assertThat(tableCount).isEqualTo(4);
			assertThat(singleCount(uniqueGrantKey.executeQuery())).isPositive();
			assertThat(singleCount(foreignKeys.executeQuery())).isEqualTo(5);
		}
	}

	private int singleCount(ResultSet result) throws Exception {
		assertThat(result.next()).isTrue();
		return result.getInt(1);
	}
}
