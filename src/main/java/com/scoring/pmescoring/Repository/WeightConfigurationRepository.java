package com.scoring.pmescoring.Repository;

import com.scoring.pmescoring.Domain.WeightConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeightConfigurationRepository extends JpaRepository<WeightConfiguration, Long> {
}
