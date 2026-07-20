package pl.backend.weddinggallery.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.user.model.SystemRole;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapServiceTest {

	@Mock
	private UserRepository users;
	@Mock
	private PasswordEncoder passwords;
	@Mock
	private AuditService audit;

	private AdminBootstrapService service;

	@BeforeEach
	void setUp() {
		service = new AdminBootstrapService(users, passwords, audit);
	}

	@Test
	void createsExactlyOneNormalizedAdministratorAndAuditsTheAction() {
		when(users.findByEmail("admin@example.com")).thenReturn(Optional.empty());
		when(users.existsBySystemRole(SystemRole.ADMIN)).thenReturn(false);
		when(passwords.encode("a-strong-password")).thenReturn("encoded");

		assertThat(service.bootstrap("  ADMIN@Example.com ", "a-strong-password")).isTrue();

		ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
		verify(users).saveAndFlush(user.capture());
		assertThat(user.getValue().getEmail()).isEqualTo("admin@example.com");
		assertThat(user.getValue().getPasswordHash()).isEqualTo("encoded");
		assertThat(user.getValue().getSystemRole()).isEqualTo(SystemRole.ADMIN);
		verify(audit).logEvent("admin@example.com", EventType.USER_REGISTERED,
				"Initial administrator bootstrapped");
	}

	@Test
	void replayForTheSameAdministratorDoesNotResetCredentialsOrAuditAgain() {
		User existing = User.builder().email("admin@example.com").systemRole(SystemRole.ADMIN).build();
		when(users.findByEmail("admin@example.com")).thenReturn(Optional.of(existing));

		assertThat(service.bootstrap("admin@example.com", "a-different-password")).isFalse();

		verify(passwords, never()).encode(any());
		verify(users, never()).saveAndFlush(any());
		verify(audit, never()).logEvent(any(), any(), any());
	}

	@Test
	void refusesToPromoteAnExistingUser() {
		User existing = User.builder().email("user@example.com").systemRole(SystemRole.USER).build();
		when(users.findByEmail("user@example.com")).thenReturn(Optional.of(existing));

		assertThatIllegalStateException().isThrownBy(() -> service.bootstrap("user@example.com", "a-strong-password"));
		verify(users, never()).saveAndFlush(any());
	}

	@Test
	void refusesToCreateAnotherAdministratorThroughBootstrap() {
		when(users.findByEmail("second@example.com")).thenReturn(Optional.empty());
		when(users.existsBySystemRole(SystemRole.ADMIN)).thenReturn(true);

		assertThatIllegalStateException()
				.isThrownBy(() -> service.bootstrap("second@example.com", "a-strong-password"));
		verify(users, never()).saveAndFlush(any());
	}

	@Test
	void rejectsInvalidEmailAndShortPassword() {
		assertThatIllegalArgumentException().isThrownBy(() -> service.bootstrap("not-an-email", "a-strong-password"));
		assertThatIllegalArgumentException().isThrownBy(() -> service.bootstrap("admin@example.com", "too-short"));
		verify(users, never()).saveAndFlush(any());
	}
}
