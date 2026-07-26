package pl.backend.weddinggallery.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import pl.backend.weddinggallery.admin.controller.AdminController;
import pl.backend.weddinggallery.admin.service.AdminService;

class AdminControllerBoundaryTest {
	@Test
	void everyAdminRouteIsDeniedWhenRoleCheckFails() {
		AdminService service = mock(AdminService.class);
		AdminController controller = new AdminController(service);
		Authentication user = mock(Authentication.class);
		when(service.isAdmin(user)).thenReturn(false);

		assertThat(controller.dashboard(user).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(controller.users(user, 0, 50).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(controller.events(user, 0, 50).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(controller.galleries(user, 0, 50).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(controller.media(user, 0, 50).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(controller.audit(user, 0, 50).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(controller.lock(user, "id").getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(controller.unlock(user, "id").getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(controller.archive(user, "id").getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(controller.hide(user, "id").getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		verify(service, times(10)).isAdmin(user);
	}

	@Test
	void paginationRejectsUnboundedAndNegativeRequests() {
		AdminService service = mock(AdminService.class);
		AdminController controller = new AdminController(service);
		Authentication admin = mock(Authentication.class);
		when(service.isAdmin(admin)).thenReturn(true);

		assertThatThrownBy(() -> controller.users(admin, -1, 50))
				.isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
		assertThatThrownBy(() -> controller.users(admin, 0, 101))
				.isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
	}

	@Test
	void everyAdminRouteForwardsWhenRoleCheckSucceeds() {
		AdminService service = mock(AdminService.class);
		AdminController controller = new AdminController(service);
		Authentication admin = mock(Authentication.class);
		when(service.isAdmin(admin)).thenReturn(true);
		when(admin.getName()).thenReturn("admin@example.com");

		controller.dashboard(admin);
		controller.users(admin, 0, 1);
		controller.events(admin, 0, 1);
		controller.galleries(admin, 0, 1);
		controller.media(admin, 0, 1);
		controller.audit(admin, 0, 1);
		controller.lock(admin, "user");
		controller.unlock(admin, "user");
		controller.archive(admin, "event");
		controller.hide(admin, "media");

		verify(service).dashboard();
		verify(service).users(any());
		verify(service).events(any());
		verify(service).galleries(any());
		verify(service).media(any());
		verify(service).audit(any());
		verify(service).lockUser("admin@example.com", "user");
		verify(service).unlockUser("admin@example.com", "user");
		verify(service).archiveEvent("admin@example.com", "event");
		verify(service).hideMedia("admin@example.com", "media");
	}
}
