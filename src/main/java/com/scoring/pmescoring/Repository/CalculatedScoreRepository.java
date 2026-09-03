package com.scoring.pmescoring.Repository;

import com.scoring.pmescoring.Domain.CalculatedScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculatedScoreRepository extends JpaRepository<CalculatedScore, Long> {
}
