package pl.backend.weddinggallery.identity.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.backend.weddinggallery.identity.domain.User;
import pl.backend.weddinggallery.identity.domain.UserRepository;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
		if (userRepository.findByEmail(request.email().toLowerCase()).isPresent()) {
			return ResponseEntity.badRequest().body("Email already in use");
		}

		User user = new User();
		user.setEmail(request.email().toLowerCase());
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		userRepository.save(user);

		return ResponseEntity.ok("User registered successfully");
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
		UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(request.email().toLowerCase(),
				request.password());
		Authentication auth = authenticationManager.authenticate(authReq);

		SecurityContext sc = SecurityContextHolder.getContext();
		sc.setAuthentication(auth);
		HttpSession session = httpRequest.getSession(true);
		session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

		return ResponseEntity.ok(new UserInfoResponse(auth.getName()));
	}

	@GetMapping("/me")
	public ResponseEntity<?> getCurrentUser(Authentication authentication) {
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
