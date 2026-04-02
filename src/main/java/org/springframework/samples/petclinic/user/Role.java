package org.springframework.samples.petclinic.user;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, unique = true, length = 50)
	private String name; // e.g., "ADMIN", "STUDENT"

	@Column(length = 255)
	private String description;

	// Mapped by the 'roles' field in the User entity.
	@ManyToMany(mappedBy = "roles")
	@EqualsAndHashCode.Exclude
	private Set<User> users;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
		name = "permission_role", // Your exact DB junction table name
		joinColumns = @JoinColumn(name = "role_id"),
		inverseJoinColumns = @JoinColumn(name = "permission_id")
	)
	@EqualsAndHashCode.Exclude
	private Set<Permission> permissions;
}