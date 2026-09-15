package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.Firm;
import com.scoring.pmescoring.dto.response.firm.FirmRiskResponse;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.model.RiskBand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FirmRepository extends JpaRepository<Firm, Long> {

    Optional<Firm> findByIdAndStatus(Long id, EntityStatus status);

    Page<Firm> findByStatus(EntityStatus status, Pageable pageable);

    boolean existsByCnpjAndStatus(String cnpj, EntityStatus status);

    @Query("SELECT new com.scoring.pmescoring.dto.response.firm.FirmRiskResponse(f.cnpj, f.registeredCompanyName, cs.scoreValue, cs.riskBand, cs.justification) " +
            "FROM Firm f JOIN CalculatedScore cs ON cs.firm.id = f.id " +
            "WHERE f.status = :firmStatus " +
            "AND cs.status = :scoreStatus " +
            "AND (:riskBand IS NULL OR cs.riskBand = :riskBand)")
    Page<FirmRiskResponse> findActiveFirmsByRiskBand(
            @Param("firmStatus") EntityStatus firmStatus,
            @Param("scoreStatus") EntityStatus scoreStatus,
            @Param("riskBand") RiskBand riskBand,
            Pageable pageable
    );
}