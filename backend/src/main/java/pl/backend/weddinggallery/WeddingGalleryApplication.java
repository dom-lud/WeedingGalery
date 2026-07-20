package pl.backend.weddinggallery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WeddingGalleryApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeddingGalleryApplication.class, args);
	}

}
