package org.springframework.samples.petclinic.school;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.samples.petclinic.model.NamedEntity;
import org.springframework.samples.petclinic.validation.UniqueDomain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schools")
@UniqueDomain
@Getter
@Setter
@SQLDelete(sql = "UPDATE schools SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class School extends NamedEntity {

	@Column(name = "domain", unique = true)
	@NotEmpty
	private String domain;

	@Enumerated(EnumType.STRING)
	@Column(name = "status_id")
	private SchoolStatus status = SchoolStatus.ACTIVE;

	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "school", fetch = FetchType.EAGER)
	private List<Location> locations = new ArrayList<>();

	public void addLocation(Location location) {
		location.setSchool(this);
		getLocations().add(location);
	}

	public enum SchoolStatus {
		ACTIVE, INACTIVE, SUSPENDED
	}

	// NEW METHOD
	public String getSlug() {
		if (this.domain == null) {
			return "";
		}
		return this.domain.replace(".edu", "");
	}
}
