package pl.backend.weddinggallery.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.bootstrap-admin.enabled", havingValue = "true")
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

	private final AdminBootstrapService adminBootstrapService;

	@Value("${app.bootstrap-admin.email:}")
	private String email;

	@Value("${app.bootstrap-admin.password:}")
	private String password;

	@Override
	public void run(ApplicationArguments args) {
		boolean created = adminBootstrapService.bootstrap(email, password);
		if (created) {
			log.info("Initial administrator account was created; disable bootstrap configuration now");
		} else {
			log.info("Initial administrator already exists; bootstrap did not modify credentials");
		}
	}
}
