package pl.backend.weddinggallery;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class DevelopmentConfigurationTest {

	@Test
	void developmentProfileUsesFlywayManagedSchema() throws IOException {
		YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
		List<PropertySource<?>> sources = loader.load("application-dev",
				new ClassPathResource("application-dev.yml"));
		PropertySource<?> properties = sources.get(0);

		assertThat(properties.getProperty("spring.datasource.url"))
				.isEqualTo("jdbc:h2:mem:devdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL");
		assertThat(properties.getProperty("spring.datasource.driver-class-name")).isEqualTo("org.h2.Driver");
		assertThat(properties.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("none");
		assertThat(properties.getProperty("spring.flyway.enabled")).isEqualTo(true);
	}
}
