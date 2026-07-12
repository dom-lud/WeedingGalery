package pl.backend.weddinggallery.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import pl.backend.weddinggallery.auth.dto.LoginRequest;
import pl.backend.weddinggallery.auth.dto.RegisterRequest;
import pl.backend.weddinggallery.user.model.User;
import pl.backend.weddinggallery.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public boolean registerUser(RegisterRequest request) {
		if (userRepository.findByEmail(request.email().toLowerCase()).isPresent()) {
			return false;
		}

		User user = new User();
		user.setEmail(request.email().toLowerCase());
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		userRepository.save(user);

		return true;
	}

	public String loginUser(LoginRequest request, HttpServletRequest httpRequest) {
		UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(
				request.email().toLowerCase(), request.password());
		Authentication auth = authenticationManager.authenticate(authReq);

		SecurityContext sc = SecurityContextHolder.getContext();
		sc.setAuthentication(auth);
		HttpSession session = httpRequest.getSession(true);
		session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

		return auth.getName();
	}
}
