package org.springframework.samples.petclinic.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.samples.petclinic.model.BaseEntity;

import java.util.Set;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
public class Permission extends BaseEntity {

	@Column(nullable = false, unique = true, length = 100)
	private String name; // e.g., "MANAGE_FACILITIES", "VIEW_LEAGUES"

	@Column(length = 255)
	private String description;

	// The reverse mapping back to the Role entity
	@ManyToMany(mappedBy = "permissions")
	@EqualsAndHashCode.Exclude
	private Set<Role> roles;
}