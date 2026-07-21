package pl.backend.weddinggallery.auth.service;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.user.model.SystemRole;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AdminBootstrapService {

	private static final int MINIMUM_PASSWORD_LENGTH = 12;
	private static final String LEGACY_ADMIN_ID = "00000000-0000-0000-0000-000000000001";
	private static final String DISABLED_LEGACY_PASSWORD = "BOOTSTRAP_DISABLED";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuditService auditService;

	@Transactional
	public boolean bootstrap(String rawEmail, String rawPassword) {
		String email = normalizeAndValidateEmail(rawEmail);
		validatePassword(rawPassword);

		var existingUser = userRepository.findByEmail(email);
		if (existingUser.isPresent()) {
			User user = existingUser.get();
			if (isDisabledLegacyAdministrator(user)) {
				user.setPasswordHash(passwordEncoder.encode(rawPassword));
				user.setFailedLoginAttempts(0);
				user.setLockedUntil(null);
				userRepository.saveAndFlush(user);
				auditService.logRequiredIdentityEvent(email, EventType.ADMIN_BOOTSTRAPPED,
						"Legacy initial administrator credential replaced");
				return true;
			}
			if (user.getSystemRole() == SystemRole.ADMIN) {
				return false;
			}
			throw new IllegalStateException("Bootstrap email belongs to a non-administrator account");
		}

		if (userRepository.existsBySystemRole(SystemRole.ADMIN)) {
			throw new IllegalStateException("An administrator already exists; bootstrap cannot create another one");
		}

		User admin = User.builder().email(email).passwordHash(passwordEncoder.encode(rawPassword))
				.systemRole(SystemRole.ADMIN).failedLoginAttempts(0).build();
		userRepository.saveAndFlush(admin);
		auditService.logRequiredIdentityEvent(email, EventType.ADMIN_BOOTSTRAPPED,
				"Initial administrator bootstrapped");
		return true;
	}

	private boolean isDisabledLegacyAdministrator(User user) {
		return LEGACY_ADMIN_ID.equals(user.getId()) && user.getSystemRole() == SystemRole.ADMIN
				&& DISABLED_LEGACY_PASSWORD.equals(user.getPasswordHash());
	}

	private String normalizeAndValidateEmail(String rawEmail) {
		if (rawEmail == null) {
			throw new IllegalArgumentException("Bootstrap administrator email is required");
		}
		String email = rawEmail.trim().toLowerCase(Locale.ROOT);
		if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
			throw new IllegalArgumentException("Bootstrap administrator email is invalid");
		}
		return email;
	}

	private void validatePassword(String password) {
		if (password == null || password.length() < MINIMUM_PASSWORD_LENGTH) {
			throw new IllegalArgumentException("Bootstrap administrator password must contain at least "
					+ MINIMUM_PASSWORD_LENGTH + " characters");
		}
	}
}
