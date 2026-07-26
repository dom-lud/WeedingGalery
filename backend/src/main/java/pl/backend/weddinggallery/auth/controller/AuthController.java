package pl.backend.weddinggallery.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.backend.weddinggallery.auth.dto.LoginRequest;
import pl.backend.weddinggallery.auth.dto.RegisterRequest;
import pl.backend.weddinggallery.auth.dto.UserInfoResponse;
import pl.backend.weddinggallery.auth.service.AuthService;
import pl.backend.weddinggallery.user.repository.UserRepository;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final UserRepository userRepository;

	public AuthController(AuthService authService) {
		this(authService, null);
	}

	@Autowired
	public AuthController(AuthService authService, UserRepository userRepository) {
		this.authService = authService;
		this.userRepository = userRepository;
	}

	@PostMapping("/register")
	public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
		authService.registerUser(request);
		return ResponseEntity.ok("User registered successfully");
	}

	@PostMapping("/login")
	public ResponseEntity<UserInfoResponse> login(@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest) {
		String username = authService.loginUser(request, httpRequest);
		return ResponseEntity.ok(userInfo(username));
	}

	@GetMapping("/me")
	public ResponseEntity<UserInfoResponse> getCurrentUser(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			return ResponseEntity.status(401).build();
		}
		return ResponseEntity.ok(userInfo(authentication.getName()));
	}

	private UserInfoResponse userInfo(String email) {
		return userRepository == null
				? new UserInfoResponse(email)
				: userRepository.findByEmail(email)
						.map(user -> new UserInfoResponse(user.getEmail(), user.getSystemRole()))
						.orElseGet(() -> new UserInfoResponse(email));
	}

	@GetMapping("/csrf")
	public ResponseEntity<Void> getCsrf(org.springframework.security.web.csrf.CsrfToken token) {
		if (token != null) {
			token.getToken(); // Wymusza faktyczne wygenerowanie i wysłanie ciastka
		}
		return ResponseEntity.ok().build();
	}
}
