package org.springframework.samples.petclinic.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserDetailsServiceImpl userDetailsService;

	private User testUser;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setEmail("example-student@kirkwood.edu");
		testUser.setPassword("hashedPassword");
		Role studentRole = new Role();
		studentRole.setName("STUDENT");
		Permission viewLeaguesPermission = new Permission();
		viewLeaguesPermission.setName("VIEW_LEAGUES");
		studentRole.setPermissions(Set.of(viewLeaguesPermission));
		testUser.setRoles(Set.of(studentRole));
	}

	@Test
	void loadUserByUsername() {
		// Arrange
		// When findByEmail is called with the test email, return the user
		when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

		// Act
		UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getEmail());

		// Assert
		assertNotNull(userDetails);
		assertEquals(testUser.getEmail(), userDetails.getUsername());
		assertEquals(testUser.getPassword(), userDetails.getPassword());
		assertTrue(userDetails.getAuthorities().stream()
			.anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));
		assertTrue(userDetails.getAuthorities().stream()
			.anyMatch(a ->a.getAuthority().equals("VIEW_LEAGUES")));
		verify(userRepository, times(1)).findByEmail(testUser.getEmail());
	}
}
