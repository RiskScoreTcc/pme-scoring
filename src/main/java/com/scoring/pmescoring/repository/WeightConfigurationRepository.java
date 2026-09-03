package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.WeightConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeightConfigurationRepository extends JpaRepository<WeightConfiguration, Long> {
}
