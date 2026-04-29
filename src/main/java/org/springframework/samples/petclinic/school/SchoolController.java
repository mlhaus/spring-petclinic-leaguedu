package org.springframework.samples.petclinic.school;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.league.League;
import org.springframework.samples.petclinic.league.LeagueRepository;
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

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/schools")
public class SchoolController {

	private final SchoolRepository schoolRepository;
	private final UserRepository userRepository;
	private final LeagueRepository leagueRepository;   // ✅ NEW

	// ✅ Inject LeagueRepository
	public SchoolController(SchoolRepository schoolRepository,
							UserRepository userRepository,
							LeagueRepository leagueRepository) {
		this.schoolRepository = schoolRepository;
		this.userRepository = userRepository;
		this.leagueRepository = leagueRepository;
	}

	@GetMapping("/new")
	public String initCreationForm(Map<String, School> model) {
		School school = new School();
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

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		boolean isSuperAdmin = auth.getAuthorities().stream()
			.anyMatch(a -> a.getAuthority().equals("MANAGE_ALL_SCHOOLS"));

		if (!isSuperAdmin) {
			school.setStatus(existingSchool.getStatus());
		}

		school.setId(id);
		schoolRepository.save(school);

		return "redirect:/schools/" + school.getSlug();
	}

	@PostMapping("/new")
	public String processCreationForm(@Valid School school, BindingResult result) {
		if (result.hasErrors()) {
			return "schools/createOrUpdateSchoolForm";
		}
		schoolRepository.save(school);
		return "redirect:/schools/" + school.getSlug();
	}

	@GetMapping
	public String showSchoolList(@RequestParam(defaultValue = "1") int page, Model model) {
		Pageable pageable = PageRequest.of(page - 1, 5);
		Page<School> schoolPage = schoolRepository.findAll(pageable);

		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", schoolPage.getTotalPages());
		model.addAttribute("totalItems", schoolPage.getTotalElements());
		model.addAttribute("listSchools", schoolPage.getContent());

		return "schools/schoolList";
	}

	// ❌ OLD METHOD REMOVED:
	// @GetMapping("/{schoolId:\\d+}") public ModelAndView showSchool(...) { ... }

	// ✅ NEW: redirect numeric ID → slug
	@GetMapping("/{schoolId:\\d+}")
	public String redirectToSlug(@PathVariable("schoolId") int schoolId) {
		School school = schoolRepository.findById(schoolId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));

		return "redirect:/schools/" + school.getSlug();
	}

	// Matches /schools/kirkwood
	@GetMapping("/{slug:[a-zA-Z0-9-]*[a-zA-Z-][a-zA-Z0-9-]*}")
	public ModelAndView showSchoolBySlug(@PathVariable("slug") String slug, Principal principal) {

		String fullDomain = slug + ".edu";

		ModelAndView mav = new ModelAndView("schools/schoolDetails");

		School school = schoolRepository.findByDomain(fullDomain)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School with domain '" + fullDomain + "' not found."));

		mav.addObject("school", school);
		mav.addObject("canEdit", checkEditPermissions(school));

		if (principal != null) {
			userRepository.findByEmail(principal.getName()).ifPresent(user -> {
				String phone = user.getPhone();
				if (phone != null && phone.length() == 10) {
					user.setPhone(phone.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "($1) $2-$3"));
				}
				mav.addObject("currentUser", user);
			});
		}

		// ✅ Fetch leagues based on permissions
		List<League> leagues;
		if (checkEditPermissions(school)) {
			leagues = leagueRepository.findBySchoolIdOrderByLeagueStartDesc(school.getId());
		} else {
			leagues = leagueRepository.findActiveLeagues(
				school.getId(),
				League.LeagueStatus.DRAFT,
				LocalDateTime.now()
			);
		}

		mav.addObject("leagues", leagues);

		return mav;
	}

	private boolean checkEditPermissions(School school) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

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
		throw new RuntimeException("This is a simulated database failure to test the 500 page stack trace.");
	}
}
