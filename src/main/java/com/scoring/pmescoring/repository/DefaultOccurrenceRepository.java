package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.DefaultOccurrence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DefaultOccurrenceRepository extends JpaRepository<DefaultOccurrence, Long> {
}
