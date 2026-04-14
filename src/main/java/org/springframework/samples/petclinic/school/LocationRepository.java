package org.springframework.samples.petclinic.school;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocationRepository extends CrudRepository<Location, Integer> {
	@Query(value = "CALL get_locations_by_school(:schoolId)", nativeQuery = true)
	List<Location> findBySchoolId(@Param("schoolId") int schoolId);
}
