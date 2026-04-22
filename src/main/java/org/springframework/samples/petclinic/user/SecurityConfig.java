package org.springframework.samples.petclinic.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		// Make the AuthenticationManager (which knows about your UserDetailsService)
		// available for injection in your controllers.
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable()) // Disable Cross-Site Request Forgery for API development
			.authorizeHttpRequests(authorize -> authorize
				// Public pages anyone can see
				.requestMatchers(
					"/",
					"/register-student",
					"/resources/**",
					"/recipes/**",
					"/recipes/new",
					"/owners/**",
					"/pets/**",
					"/vets/**",
					"/vets.html",
					"/forgot-password",
					"/reset-password"
				).permitAll()
				// Only SUPER_ADMIN users can add new schools
				.requestMatchers("/schools/new").hasAuthority("MANAGE_ALL_SCHOOLS")
				// All users can access the list of schools and individual schools
				.requestMatchers(HttpMethod.GET,
					"/schools",
					"/schools/{schoolId:\\d+}",
					"/schools/{slug:[a-zA-Z0-9-]*[a-zA-Z-][a-zA-Z0-9-]*}").permitAll()


				// Require login for the profile and any other user settings
				.requestMatchers("/users/profile", "/users/delete").authenticated()

				// PROTECTED CATCH-ALL (This protects unlisted POST/PUT/DELETE, etc.)
				.anyRequest().authenticated()
			)
			// Ensure all auto-challenge mechanisms are disabled
			.httpBasic(AbstractHttpConfigurer::disable) // Disable the login popup
			.formLogin(form -> form
				.loginPage("/login") // Tells Spring where your custom HTML is
				.usernameParameter("email") // Tells your security configuration to look for email instead of username.
				.defaultSuccessUrl("/login-success", true) // Where to go after successful login
				.failureHandler((request, response, exception) -> {
					request.getSession().setAttribute("LAST_EMAIL", request.getParameter("email"));
					response.sendRedirect("/login?error");
				})
				.permitAll()
			)
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login?logout") // Triggers the green alert box
				.permitAll()
			);

		return http.build();
	}
}
