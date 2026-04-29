package org.springframework.samples.petclinic.league;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface LeagueRepository extends JpaRepository<League, Integer> {

	List<League> findBySchoolIdOrderByLeagueStartDesc(Integer schoolId);

	@Query("SELECT l FROM League l WHERE l.school.id = :schoolId " +
		"AND l.status <> :draftStatus " +
		"AND (l.leagueEnd IS NULL OR l.leagueEnd > :now) " +
		"ORDER BY l.leagueStart ASC")
	List<League> findActiveLeagues(@Param("schoolId") Integer schoolId,
								   @Param("draftStatus") League.LeagueStatus draftStatus,
								   @Param("now") LocalDateTime now);
}
