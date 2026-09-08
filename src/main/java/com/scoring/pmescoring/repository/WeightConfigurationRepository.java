package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.WeightConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeightConfigurationRepository extends JpaRepository<WeightConfiguration, Long> {
    boolean existsByIdAndActiveTrue(Long id);
    Optional<WeightConfiguration> findByIdAndActiveTrue(Long id);
    List<WeightConfiguration> findByActiveTrue();
    long countByActiveTrue();
    Optional<WeightConfiguration> findFirstByActiveTrueOrderByIdDesc();
}
