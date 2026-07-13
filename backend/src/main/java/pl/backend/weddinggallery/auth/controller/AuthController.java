package pl.backend.weddinggallery.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
		authService.registerUser(request);
		return ResponseEntity.ok("User registered successfully");
	}

	@PostMapping("/login")
	public ResponseEntity<UserInfoResponse> login(@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest) {
		String username = authService.loginUser(request, httpRequest);
		return ResponseEntity.ok(new UserInfoResponse(username));
	}

	@GetMapping("/me")
	public ResponseEntity<UserInfoResponse> getCurrentUser(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			return ResponseEntity.status(401).build();
		}
		return ResponseEntity.ok(new UserInfoResponse(authentication.getName()));
	}

	@GetMapping("/csrf")
	public ResponseEntity<Void> getCsrf(org.springframework.security.web.csrf.CsrfToken token) {
		if (token != null) {
			token.getToken(); // Wymusza faktyczne wygenerowanie i wysłanie ciastka
		}
		return ResponseEntity.ok().build();
	}
}
