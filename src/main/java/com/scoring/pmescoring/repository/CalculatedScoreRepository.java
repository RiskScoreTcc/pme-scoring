package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.CalculatedScore;
import com.scoring.pmescoring.dto.projection.RiskBandCountDTO;
import com.scoring.pmescoring.model.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalculatedScoreRepository extends JpaRepository<CalculatedScore, Long> {

    Optional<CalculatedScore> findByIdAndStatus(Long id, EntityStatus status);

    long countByStatus(EntityStatus status);

    List<CalculatedScore> findByFirmIdAndStatus(Long firmId, EntityStatus status);

    Page<CalculatedScore> findByStatusNot(EntityStatus status, Pageable pageable);

    Optional<CalculatedScore> findByIdAndStatusNot(Long id, EntityStatus entityStatus);

    Optional<CalculatedScore> findTopByFirmIdAndStatusOrderByIdDesc(Long id, EntityStatus entityStatus);

    @Query("""
        SELECT new com.scoring.pmescoring.dto.projection.RiskBandCountDTO(cs.riskBand, COUNT(cs))
        FROM CalculatedScore cs
        WHERE cs.status = com.scoring.pmescoring.model.EntityStatus.ACTIVE
          AND cs.firm.status = com.scoring.pmescoring.model.EntityStatus.ACTIVE
          AND cs.id = (
              SELECT MAX(sub.id)
              FROM CalculatedScore sub
              WHERE sub.firm.id = cs.firm.id
                AND sub.status = com.scoring.pmescoring.model.EntityStatus.ACTIVE
          )
        GROUP BY cs.riskBand
    """)
    List<RiskBandCountDTO> countActiveFirmsByRiskBand();
}