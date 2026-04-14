package org.springframework.samples.petclinic.school;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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


}