package pl.backend.weddinggallery;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Contract test: weryfikuje, że /actuator/health jest dostępny bez autoryzacji
 * i zwraca status UP.
 *
 * <p>
 * Chroni foundation invariant: health endpoint musi być stabilny i dostępny
 * publicznie (bez auth) na potrzeby smoke testów po deploymentcie i
 * monitoringu.
 *
 * <p>
 * Spring Boot 4.x: używa @LocalServerPort + RestTemplate. TestRestTemplate i
 * AutoConfigureMockMvc zostały usunięte w SB4.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ActuatorHealthContractTest {

	@LocalServerPort
	private int port;

	private final RestTemplate restTemplate = new RestTemplate();

	@Test
	void healthEndpointIsAccessibleWithoutAuthentication() {
		ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/actuator/health",
				String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void healthEndpointReturnsUpStatus() {
		ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/actuator/health",
				String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("\"status\":\"UP\"");
	}
}
