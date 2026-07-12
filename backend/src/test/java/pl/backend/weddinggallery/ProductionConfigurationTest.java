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
				.isEqualTo("jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:wedding_gallery}");
		assertThat(properties.getProperty("spring.datasource.driver-class-name")).isEqualTo("com.mysql.cj.jdbc.Driver");
		assertThat(properties.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
		assertThat(properties.getProperty("server.port")).isEqualTo(8080);
	}
}
