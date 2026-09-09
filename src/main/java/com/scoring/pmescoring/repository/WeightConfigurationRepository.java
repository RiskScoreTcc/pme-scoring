package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.WeightConfiguration;
import com.scoring.pmescoring.model.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeightConfigurationRepository extends JpaRepository<WeightConfiguration, Long> {

    Optional<WeightConfiguration> findByIdAndStatus(Long id, EntityStatus status);

    List<WeightConfiguration> findByStatus(EntityStatus status);

    long countByStatus(EntityStatus status);

    Optional<WeightConfiguration> findFirstByStatusOrderByIdDesc(EntityStatus status);
}