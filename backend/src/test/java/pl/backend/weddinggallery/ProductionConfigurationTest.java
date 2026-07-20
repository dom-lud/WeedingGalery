package pl.backend.weddinggallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class ProductionConfigurationTest {

	@Test
	void productionProfileUsesMysqlAndValidatedSchema() throws IOException {
		YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
		List<PropertySource<?>> sources = loader.load("application-prod",
				new ClassPathResource("application-prod.yml"));
		PropertySource<?> properties = sources.get(0);

		assertThat(properties.getProperty("spring.datasource.url"))
				.isEqualTo("jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}");
		assertThat(properties.getProperty("spring.datasource.username")).isEqualTo("${DB_USER}");
		assertThat(properties.getProperty("spring.datasource.password")).isEqualTo("${DB_PASSWORD}");
		assertThat(properties.getProperty("spring.datasource.driver-class-name")).isEqualTo("com.mysql.cj.jdbc.Driver");
		assertThat(properties.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
		assertThat(properties.getProperty("server.port")).isEqualTo(8080);
		assertThat(properties.getProperty("server.forward-headers-strategy")).isEqualTo("framework");
		assertThat(properties.getProperty("server.servlet.session.cookie.secure"))
				.isEqualTo("${SESSION_COOKIE_SECURE:true}");
		assertThat(properties.getProperty("server.servlet.session.cookie.http-only")).isEqualTo(true);
		assertThat(properties.getProperty("server.servlet.session.cookie.same-site")).isEqualTo("lax");
		assertThat(properties.getProperty("app.security.cors.allowed-origin-patterns"))
				.isEqualTo("${CORS_ALLOWED_ORIGIN_PATTERNS:}");
		assertThat(properties.getProperty("app.storage.local.root")).isEqualTo("${STORAGE_LOCAL_ROOT}");
	}
}
