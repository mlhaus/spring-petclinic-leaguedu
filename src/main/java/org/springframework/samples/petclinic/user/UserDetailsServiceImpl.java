package org.springframework.samples.petclinic.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}


	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		// 1. Find the user via the UserRepository
		User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("No user found with email '" + email + "'.")); // TODO: Make the message generic
		
		// 2. Block deleted accounts
		if(user.getDeletedAt() != null) {
			throw new UsernameNotFoundException("No user found with email '" + email + "'.");
		}
		
		// 3. Convert your custom User model into the UserDetails object that Spring Security understands
		return org.springframework.security.core.userdetails.User.builder()
			.username(user.getEmail())
			.password(user.getPassword()) // Spring Security will compare this HASH with the login password
			.roles(user.getRoles().stream()
				.map(role -> role.getName())
				.toArray(String[]::new)) // Converts your Role set into Spring's required format
			.build();
	}
}
