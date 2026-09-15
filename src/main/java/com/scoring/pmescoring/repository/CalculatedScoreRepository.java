package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.CalculatedScore;
import com.scoring.pmescoring.model.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalculatedScoreRepository extends JpaRepository<CalculatedScore, Long> {

    Optional<CalculatedScore> findByIdAndStatus(Long id, EntityStatus status);

    long countByStatus(EntityStatus status);

    List<CalculatedScore> findByFirmIdAndStatus(Long firmId, EntityStatus status);

    Page<CalculatedScore> findByStatusNot(EntityStatus status, Pageable pageable);
}