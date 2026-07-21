package pl.backend.weddinggallery.security.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import pl.backend.weddinggallery.security.handler.AuditLogoutSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	@Value("${app.security.csrf.enabled:true}")
	private boolean csrfEnabled;

	@Value("${app.security.secure-cookies:false}")
	private boolean secureCookies;

	@Value("${app.security.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}")
	private List<String> allowedOriginPatterns;

	private final AuditLogoutSuccessHandler auditLogoutSuccessHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

		if (csrfEnabled) {
			CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
			csrfRepository.setCookieCustomizer(cookie -> cookie.secure(secureCookies).sameSite("Lax"));
			http.csrf(csrf -> csrf.csrfTokenRepository(csrfRepository)
					.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));
		} else {
			http.csrf(AbstractHttpConfigurer::disable);
		}

		http.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.authorizeHttpRequests(
						auth -> auth.requestMatchers("/api/auth/login", "/api/auth/csrf", "/api/public/**").permitAll()
								.requestMatchers(HttpMethod.POST, "/api/auth/register").hasRole("ADMIN")
								.requestMatchers("/actuator/health").permitAll().anyRequest().authenticated())
				.logout(logout -> logout.logoutUrl("/api/auth/logout").logoutSuccessHandler(auditLogoutSuccessHandler)
						.invalidateHttpSession(true).deleteCookies("JSESSIONID"));

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(
			org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config)
			throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		// Zezwolenie na lokalny frontend. W przyszłości można to przenieść do
		// application.yml.
		configuration.setAllowedOriginPatterns(
				allowedOriginPatterns.stream().map(String::trim).filter(pattern -> !pattern.isBlank()).toList());
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
