package org.springframework.samples.petclinic.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/users")
public class ProfileController {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserDetailsService userDetailsService;

	public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.userDetailsService = userDetailsService;
	}

	@ModelAttribute("languageOptions")
	public Map<String, String> populateLanguageOptions() {
		Map<String, String> options = new LinkedHashMap<>();
		options.put("en", "English");
		options.put("ko", "Korean");
		options.put("es", "Spanish");
		return options;
	}

	@GetMapping("/profile")
	public String showProfileForm(Model model, Principal principal) {
		String email = principal.getName();
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("User not found"));
		user.setPassword(""); // This will make the password field blank by default

		// Intercept the 10-digit database string and inject a parentheses
		// and hyphen before handing it to Thymeleaf
		String phone = user.getPhone();
		if (phone != null && phone.length() == 10) {
			// Converts 3199999999 into (319) 999-9999
			String formattedPhone = phone.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "($1) $2-$3");
			user.setPhone(formattedPhone);
		}

		String domain = email.substring(email.indexOf("@") + 1); // kirkwood.edu
		String slug = domain.contains(".") ? domain.substring(0, domain.lastIndexOf(".")) : domain; // kirkwood
		slug = slug.contains(".") ? slug.substring(slug.indexOf(".") + 1) : slug;
		model.addAttribute("schoolSlug", slug);

		model.addAttribute("user", user);
		return "users/profile";
	}

	@PostMapping("/profile")
	public String updateProfile(@Valid @ModelAttribute("user") User updatedUser,
								BindingResult result,
								Principal principal,
								RedirectAttributes redirectAttributes) {
		String currentEmail = principal.getName();
		User currentUser = userRepository.findByEmail(currentEmail).orElseThrow(() -> new RuntimeException("User not found"));

		// 1. Is the user trying to change their email?
		if(!currentEmail.equalsIgnoreCase(updatedUser.getEmail())) {
			// Does the new email address exist in the database?
			if(userRepository.existsByEmail(updatedUser.getEmail())) {
				result.rejectValue("email", "duplicateEmail", "This email is already taken");
			}
		}

		// 2. Validate password strength manually
		// Why? Because the registration validation is required in all cases, wheras this one is not.
		String newPassword = updatedUser.getPassword();
		boolean isUpdatingPassword = newPassword != null && !newPassword.trim().isEmpty();

		if (isUpdatingPassword) {
			if (!newPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")) {
				// Add this regex check to enforce the character rules
				result.rejectValue("password", "weakPassword", "Password must be at least 8 characters and must contain uppercase, lowercase, and number");
			}
		}

		if(result.hasErrors()) {
			return "/users/profile"; // Don't use "redirect", making a GET request will not display errors
		}

		currentUser.setFirstName(updatedUser.getFirstName());
		currentUser.setLastName(updatedUser.getLastName());
		currentUser.setNickname(updatedUser.getNickname());
		currentUser.setEmail(updatedUser.getEmail());

		String submittedPhone = updatedUser.getPhone();
		if (submittedPhone != null && !submittedPhone.trim().isEmpty()) {
			currentUser.setPhone(submittedPhone.replaceAll("\\D", "")); // Strips all non-numbers
		} else {
			currentUser.setPhone(null);
		}

		currentUser.setPublicEmail(updatedUser.getPublicEmail());
		currentUser.setPublicPhone(updatedUser.getPublicPhone());
		currentUser.setPreferredLanguage(updatedUser.getPreferredLanguage());

		if (isUpdatingPassword) {
			currentUser.setPassword(passwordEncoder.encode(newPassword));
		}

		// If no errors, update the database and redirect the user.
		userRepository.save(currentUser);

		// 6. Update the Spring Security Context if the email changed
		if (!currentEmail.equalsIgnoreCase(currentUser.getEmail())) {

			// Fetch the freshly updated user details
			UserDetails newPrincipal = userDetailsService.loadUserByUsername(currentUser.getEmail());
			Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

			// Create a new authentication token with the new email
			Authentication newAuth = new UsernamePasswordAuthenticationToken(
				newPrincipal,
				currentAuth.getCredentials(),
				newPrincipal.getAuthorities());

			// Replace the old token in the session memory
			SecurityContextHolder.getContext().setAuthentication(newAuth);
		}

		redirectAttributes.addFlashAttribute("messageSuccess", "Your profile has been updated successfully");

		String langCode = currentUser.getPreferredLanguage();
		if(langCode != null) {
			return "redirect:/users/profile?lang=" + langCode.toLowerCase();
		}

		return "redirect:/users/profile"; // This makes a new GET request
	}

	@PostMapping("/delete")
	public String deleteAccount(Principal principal,
								HttpServletRequest request,
								HttpServletResponse response,
								RedirectAttributes redirectAttributes) {
		// Finding out who is logged in
		String email = principal.getName();
		// Get all of their data from the datbaase
		User currentUser = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
		// Mark them as deleted
		currentUser.setDeletedAt(LocalDateTime.now());
		currentUser.setEmail("deleted_" + currentUser.getId() + email);
		// Update the database
		userRepository.save(currentUser);
		// Log the user out
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		// Redirect to the homepage with a farewall message
		redirectAttributes.addFlashAttribute("messageSuccess", "Your account has been successfully deleted. We're sorry to see you go!");
		return "redirect:/";
	}


}
