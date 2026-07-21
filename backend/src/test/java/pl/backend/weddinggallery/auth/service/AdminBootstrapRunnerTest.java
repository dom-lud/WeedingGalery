package pl.backend.weddinggallery.auth.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

class AdminBootstrapRunnerTest {

	@Test
	void delegatesConfiguredCredentialsForProvisioningAndReplay() {
		AdminBootstrapService service = mock(AdminBootstrapService.class);
		AdminBootstrapRunner runner = new AdminBootstrapRunner(service);
		ReflectionTestUtils.setField(runner, "email", "admin@example.com");
		ReflectionTestUtils.setField(runner, "password", "strong-password");
		when(service.bootstrap("admin@example.com", "strong-password")).thenReturn(true, false);

		runner.run(new DefaultApplicationArguments());
		runner.run(new DefaultApplicationArguments());

		verify(service, times(2)).bootstrap("admin@example.com", "strong-password");
	}
}
