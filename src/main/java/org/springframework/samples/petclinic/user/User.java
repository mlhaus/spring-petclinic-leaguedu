package org.springframework.samples.petclinic.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.validation.OnRegister;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
// Intercept the delete command and turn it into an update
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
// Automatically filter out deleted rows when reading data
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity {

	@Column(name="first_name", nullable = true, length = 50)
	private String firstName;

	@Column(name="last_name", nullable = true, length = 50)
	private String lastName;

	@Column(name = "nickname", length = 50)
	private String nickname;

	@Column(name = "nickname_is_flagged")
	private Boolean nicknameIsFlagged;

	@Column(nullable = false, unique = true, length = 255)
	@NotEmpty(message = "Email is required")
	@Email(message = "Please enter a valid email")
	private String email;

	@Column(name = "public_email")
	private Boolean publicEmail;

	@Column(name = "phone", length = 255)
	@Pattern(regexp = "^$|^(?:\\+\\d{1,3}\\s?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$",
		message = "Please enter a valid phone number")
	private String phone;

	@Column(name = "public_phone")
	private Boolean publicPhone;

	@Column(name = "preferred_language", length = 50)
	private String preferredLanguage;

	@Column(name="password_hash", nullable = true, length = 255)
	@NotEmpty(message = "Password is required", groups = OnRegister.class)
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
		message = "Password must be at least 8 characters and must contain uppercase, lowercase, and number",
		groups = OnRegister.class
	)
	private String password;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

//	@Column(name = "reset_token", unique = true)
//	private String resetToken;
//
//	@Column(name = "reset_token_expires_at")
//	private LocalDateTime resetTokenExpiresAt;

	// Optional helper methods for cleaner controller logic
//	public boolean isResetTokenValid() {
//		return this.resetTokenExpiresAt != null && LocalDateTime.now().isBefore(this.resetTokenExpiresAt);
//	}

//	public void clearResetToken() {
//		this.resetToken = null;
//		this.resetTokenExpiresAt = null;
//	}


	// Many-to-Many Relationship with Role
	@ManyToMany(fetch = FetchType.EAGER) // Fetch roles immediately when a user is loaded
	@JoinTable(
		name = "user_roles", // Name of the junction table in MySQL
		joinColumns = @JoinColumn(name = "user_id"), // Column in user_roles that references the 'users' table
		inverseJoinColumns = @JoinColumn(name = "role_id") // Column in user_roles that references the 'roles' table
	)
	@EqualsAndHashCode.Exclude
	private Set<Role> roles;
}
