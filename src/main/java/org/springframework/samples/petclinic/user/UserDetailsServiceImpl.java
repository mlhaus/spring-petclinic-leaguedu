package org.springframework.samples.petclinic.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

		List<GrantedAuthority> authorities = new ArrayList<>();

		for(Role role: user.getRoles()) {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

			for(Permission permission: role.getPermissions()) {
				authorities.add(new SimpleGrantedAuthority(permission.getName()));
			}
		}

		// 3. Convert your custom User model into the UserDetails object that Spring Security understands
		return org.springframework.security.core.userdetails.User.builder()
			.username(user.getEmail())
			.password(user.getPassword()) // Spring Security will compare this HASH with the login password
			.authorities(authorities)
			.build();
	}
}
