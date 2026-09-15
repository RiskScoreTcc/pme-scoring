package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.Firm;
import com.scoring.pmescoring.model.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FirmRepository extends JpaRepository<Firm, Long> {

    Optional<Firm> findByIdAndStatus(Long id, EntityStatus status);

    Page<Firm> findByStatus(EntityStatus status, Pageable pageable);

    boolean existsByCnpjAndStatus(String cnpj, EntityStatus status);
}