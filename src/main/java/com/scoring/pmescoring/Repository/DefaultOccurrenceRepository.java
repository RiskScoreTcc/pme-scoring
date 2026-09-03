package com.scoring.pmescoring.Repository;

import com.scoring.pmescoring.Domain.DefaultOccurrence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DefaultOccurrenceRepository extends JpaRepository<DefaultOccurrence, Long> {
}
