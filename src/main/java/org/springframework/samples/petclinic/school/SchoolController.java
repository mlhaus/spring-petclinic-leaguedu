package org.springframework.samples.petclinic.school;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.user.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/schools")
public class SchoolController {
	private final SchoolRepository schoolRepository;
	private final UserRepository userRepository;

	public SchoolController(SchoolRepository schoolRepository, UserRepository userRepository) {
		this.schoolRepository = schoolRepository;
		this.userRepository = userRepository;
	}

	@GetMapping("/new")
	public String initCreationForm(Map<String, School> model) {
		// Instaniate a default object
		School school = new School();
		// Add school to input model so Thymeleaf can bind data to it
		model.put("school", school);
		return "schools/createOrUpdateSchoolForm";
	}

	@GetMapping("/{id}/edit")
	public String initUpdateForm(@PathVariable int id, Model model) {
		School school = schoolRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));
		verifyEditPermissions(school);
		model.addAttribute("school", school);
		return "schools/createOrUpdateSchoolForm";
	}

	@PostMapping("/{id}/edit")
	public String processUpdateForm(@Valid School school, BindingResult result, @PathVariable("id") int id) {
		School existingSchool = schoolRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));

		verifyEditPermissions(existingSchool);

		if (result.hasErrors()) {
			return "schools/createOrUpdateSchoolForm";
		}

		// Prevent standard admins from modifying the status via form tampering
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		boolean isSuperAdmin = auth.getAuthorities().stream()
			.anyMatch(a -> a.getAuthority().equals("MANAGE_ALL_SCHOOLS"));

		if (!isSuperAdmin) {
			school.setStatus(existingSchool.getStatus());
		}

		school.setId(id);
		schoolRepository.save(school);

		// Strip ".edu" for the redirect to match your slug regex [a-zA-Z-]+
		String slug = school.getDomain().replace(".edu", "");
		return "redirect:/schools/" + slug;
	}

	@PostMapping("/new")
	public String processCreationForm(@Valid School school, BindingResult result) {
		if (result.hasErrors()) {
			return "schools/createOrUpdateSchoolForm";
		}
		schoolRepository.save(school);
		String slug = school.getDomain().replace(".edu", "");
		return "redirect:/schools/" + slug;
	}


	@GetMapping
	public String showSchoolList(@RequestParam(defaultValue = "1") int page, Model model) {
		// Pagination setup (5 items per page)
		Pageable pageable = PageRequest.of(page - 1, 5);
		Page<School> schoolPage = schoolRepository.findAll(pageable);

		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", schoolPage.getTotalPages());
		model.addAttribute("totalItems", schoolPage.getTotalElements());
		model.addAttribute("listSchools", schoolPage.getContent());

		return "schools/schoolList";
	}

	// Matches /schools/1
	@GetMapping("/{schoolId:\\d+}")
	public ModelAndView showSchool(@PathVariable("schoolId") int schoolId) {
		ModelAndView mav = new ModelAndView("schools/schoolDetails");
		School school = schoolRepository.findById(schoolId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School with id " + schoolId + " not found."));
		mav.addObject(school);
		mav.addObject("canEdit", checkEditPermissions(school));
		return mav;
	}

	// Matches /schools/kirkwood

	@GetMapping("/{slug:[a-zA-Z0-9-]*[a-zA-Z-][a-zA-Z0-9-]*}")
	public ModelAndView showSchoolBySlug(@PathVariable("slug") String slug, Principal principal) {
		// Reconstruct the domain (User asked to assume ".edu")
		String fullDomain = slug + ".edu";
		ModelAndView mav = new ModelAndView("schools/schoolDetails");
		School school = schoolRepository.findByDomain(fullDomain)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School with domain '" + fullDomain + "' not found."));
		mav.addObject(school);
		mav.addObject("canEdit", checkEditPermissions(school));
		if (principal != null) {
			userRepository.findByEmail(principal.getName()).ifPresent(user -> {

				// Format the 10-digit database phone number for display
				String phone = user.getPhone();
				if (phone != null && phone.length() == 10) {
					user.setPhone(phone.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "($1) $2-$3"));
				}

				mav.addObject("currentUser", user);
			});
		}

		return mav;
	}

	private boolean checkEditPermissions(School school) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// Handle unauthenticated users safely
		if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
			return false;
		}

		String userEmail = auth.getName();

		boolean isSuperAdmin = auth.getAuthorities().stream()
			.anyMatch(a -> a.getAuthority().equals("MANAGE_ALL_SCHOOLS"));
		boolean isSchoolAdmin = auth.getAuthorities().stream()
			.anyMatch(a -> a.getAuthority().equals("MANAGE_FACILITIES"));

		boolean belongsToSchool = userEmail.endsWith("@" + school.getDomain()) ||
			userEmail.endsWith("." + school.getDomain());

		return isSuperAdmin || (isSchoolAdmin && belongsToSchool);
	}

	private void verifyEditPermissions(School school) {
		if (!checkEditPermissions(school)) {
			throw new AccessDeniedException("You do not have permission to edit this school.");
		}
	}

	@GetMapping("/bad")
	public void badSchoolRequest() {
		if (true) {
			throw new RuntimeException("This is a simulated database failure to test the 500 page stack trace.");
		}
	}

}
