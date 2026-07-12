package pl.backend.weddinggallery;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootTest
class WeddingGalleryApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private Environment environment;

	@Test
	void contextLoads() {
		assertThat(applicationContext).isNotNull();
	}

	@Test
	void usesCanonicalApplicationName() {
		assertThat(environment.getProperty("spring.application.name")).isEqualTo("WeddingGallery");
	}

}
