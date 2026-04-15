package org.springframework.samples.petclinic.school;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("/schools/{schoolId}/locations")
public class LocationController {

	private final SchoolRepository schoolRepository;
	private final LocationRepository locationRepository;

	public LocationController(SchoolRepository schoolRepository, LocationRepository locationRepository) {
		this.schoolRepository = schoolRepository;
		this.locationRepository = locationRepository;
	}

	// This executes before every route in this controller to populate the school and check permissions
	@ModelAttribute("school")
	public School findSchoolAndVerifyPermissions(@PathVariable("schoolId") int schoolId) {
		School school = schoolRepository.findById(schoolId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
			throw new AccessDeniedException("Authentication required.");
		}

		String userEmail = auth.getName();
		boolean isSuperAdmin = auth.getAuthorities().stream()
			.anyMatch(a -> a.getAuthority().equals("MANAGE_ALL_SCHOOLS"));
		boolean isSchoolAdmin = auth.getAuthorities().stream()
			.anyMatch(a -> a.getAuthority().equals("MANAGE_FACILITIES"));

		boolean belongsToSchool = userEmail.endsWith("@" + school.getDomain()) ||
			userEmail.endsWith("." + school.getDomain());

		if (!isSuperAdmin && !(isSchoolAdmin && belongsToSchool)) {
			throw new AccessDeniedException("You do not have permission to manage facilities for this school.");
		}

		return school;
	}

	// Injects the list of potential parent locations into the Thymeleaf model
	@ModelAttribute("parentLocations")
	public List<Location> populateParentLocations(School school, @PathVariable(required = false) Integer locationId) {
		return school.getLocations().stream()
			// 1. Filter out the blank, unsaved location used for form binding
			.filter(loc -> loc.getId() != null)
			// 2. Filter out the current location being edited to prevent infinite loops
			.filter(loc -> locationId == null || !loc.getId().equals(locationId))
			.toList();
	}

	@GetMapping("/new")
	public String initCreationForm(School school, ModelMap model) {
		Location location = new Location();
		school.addLocation(location);
		model.put("location", location);
		return "schools/createOrUpdateLocationForm";
	}

	@PostMapping("/new")
	public String processCreationForm(School school, @Valid Location location, BindingResult result, ModelMap model) {
		if (result.hasErrors()) {
			model.put("location", location);
			return "schools/createOrUpdateLocationForm";
		}
		school.addLocation(location);
		locationRepository.save(location);

		String slug = school.getDomain().replace(".edu", "");
		return "redirect:/schools/" + slug;
	}

	@GetMapping("/{locationId}/edit")
	public String initUpdateForm(@PathVariable("locationId") int locationId, ModelMap model) {
		Location location = locationRepository.findById(locationId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
		model.put("location", location);
		return "schools/createOrUpdateLocationForm";
	}

	@PostMapping("/{locationId}/edit")
	public String processUpdateForm(@Valid Location location, BindingResult result, School school,
	                                @PathVariable("locationId") int locationId, ModelMap model) {
		if (result.hasErrors()) {
			location.setId(locationId);
			model.put("location", location);
			return "schools/createOrUpdateLocationForm";
		}
		location.setId(locationId);
		school.addLocation(location);
		locationRepository.save(location);

		String slug = school.getDomain().replace(".edu", "");
		return "redirect:/schools/" + slug;
	}

	@PostMapping("/{locationId}/delete")
	@Transactional
	public String processDeleteForm(@PathVariable("schoolId") int schoolId, @PathVariable("locationId") int locationId) {
		Location location = locationRepository.findById(locationId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));

		School school = location.getSchool();

		// 1. Sever the parent-to-child link
		if (school != null) {
			school.getLocations().removeIf(loc -> loc.getId() != null && loc.getId().equals(locationId));
		}

		// 2. Sever the child-to-parent link
		location.setSchool(null);

		// 3. Execute the soft-delete within the transaction
		locationRepository.delete(location);

		return "redirect:/schools/" + schoolId;
	}


}
