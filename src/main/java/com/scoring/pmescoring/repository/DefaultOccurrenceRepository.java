package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.DefaultOccurrence;
import com.scoring.pmescoring.model.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DefaultOccurrenceRepository extends JpaRepository<DefaultOccurrence, Long> {

    Optional<DefaultOccurrence> findByIdAndStatus(Long id, EntityStatus status);

    Page<DefaultOccurrence> findByStatus(EntityStatus status, Pageable pageable);

    List<DefaultOccurrence> findByFirmIdAndStatusAndStatusResolvedFalse(Long firmId, EntityStatus status);

    List<DefaultOccurrence> findByFirmIdAndStatus(Long firmId, EntityStatus status);
}