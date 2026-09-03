package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.CalculatedScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculatedScoreRepository extends JpaRepository<CalculatedScore, Long> {
}
