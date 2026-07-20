package pl.backend.weddinggallery.auth.service;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.backend.weddinggallery.audit.model.EventType;
import pl.backend.weddinggallery.audit.service.AuditService;
import pl.backend.weddinggallery.user.model.SystemRole;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AdminBootstrapService {

	private static final int MINIMUM_PASSWORD_LENGTH = 12;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuditService auditService;

	public boolean bootstrap(String rawEmail, String rawPassword) {
		String email = normalizeAndValidateEmail(rawEmail);
		validatePassword(rawPassword);

		var existingUser = userRepository.findByEmail(email);
		if (existingUser.isPresent()) {
			if (existingUser.get().getSystemRole() == SystemRole.ADMIN) {
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
		auditService.logEvent(email, EventType.USER_REGISTERED, "Initial administrator bootstrapped");
		return true;
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
