package org.springframework.samples.petclinic.school;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.samples.petclinic.model.NamedEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "locations")
@Getter // Auto-generates getters for all fields
@Setter // Auto-generates setters for all fields
@SQLDelete(sql = "UPDATE locations SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Location extends NamedEntity {

	@ManyToOne
	@JoinColumn(name = "school_id")
	private School school;

	@ManyToOne
	@JoinColumn(name = "parent_location_id")
	private Location parentLocation;

	@Column(name = "description")
	private String description;

	@Column(name = "address")
	@NotBlank(message = "Please provide a street address.")
	private String address;

	@Column(name = "latitude")
	@NotNull(message = "Latitude is required.")
	@DecimalMin(value = "-90.0", message = "Latitude must be a valid number between -90 and 90.")
	@DecimalMax(value = "90.0", message = "Latitude must be a valid number between -90 and 90.")
	private BigDecimal latitude;

	@Column(name = "longitude")
	@NotNull(message = "Longitude is required.")
	@DecimalMin(value = "-180.0", message = "Longitude must be a valid number between -180 and 180.")
	@DecimalMax(value = "180.0", message = "Longitude must be a valid number between -180 and 180.")
	private BigDecimal longitude;

	@Enumerated(EnumType.STRING)
	@Column(name = "status_id")
	@NotNull(message = "Please select a valid status.")
	private LocationStatus status = LocationStatus.ACTIVE;
	
	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public enum LocationStatus {
		DRAFT, ACTIVE, CLOSED, COMING_SOON;
	}

	// No more manual code below this line!
}
