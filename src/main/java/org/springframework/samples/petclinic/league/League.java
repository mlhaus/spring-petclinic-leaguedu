package org.springframework.samples.petclinic.league;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.samples.petclinic.model.NamedEntity;
import org.springframework.samples.petclinic.school.Location;
import org.springframework.samples.petclinic.school.School;
import org.springframework.samples.petclinic.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leagues")
@Getter
@Setter
@SQLDelete(sql = "UPDATE leagues SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class League extends NamedEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "school_id", nullable = false)
	private School school;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "location_id")
	private Location defaultLocation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User manager;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "copied_from_id")
	private League copiedFrom;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "registration_start")
	private LocalDateTime registrationStart;

	@Column(name = "registration_end")
	private LocalDateTime registrationEnd;

	@Column(name = "league_start")
	private LocalDateTime leagueStart;

	@Column(name = "league_end")
	private LocalDateTime leagueEnd;

	@Setter
	@Column(name = "is_public")
	private boolean isPublic = true;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private LeagueType type;

	@NotNull
	@Min(1)
	@Column(name = "capacity")
	private Integer capacity;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "capacity_type", nullable = false)
	private CapacityType capacityType;

	@NotNull
	@DecimalMin(value = "0.00")
	@Column(name = "fee", precision = 6, scale = 2)
	private BigDecimal fee;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "status_id")
	private LeagueStatus status = LeagueStatus.DRAFT;

	// Enums
	public enum LeagueType { MALE, FEMALE, COED }
	public enum CapacityType { TEAM, INDIVIDUAL }
	public enum LeagueStatus { DRAFT, ACTIVE, INACTIVE, POSTPONED, CANCELLED, PAST }

	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	// ✅ REQUIRED for Thymeleaf boolean binding
	public boolean getIsPublic() {
		return isPublic;
	}

}
