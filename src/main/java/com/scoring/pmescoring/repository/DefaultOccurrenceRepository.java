package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.DefaultOccurrence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DefaultOccurrenceRepository extends JpaRepository<DefaultOccurrence, Long> {
    Optional<DefaultOccurrence> findByIdAndActiveTrue(Long id);
    Page<DefaultOccurrence> findByActiveTrue(Pageable pageable);
    List<DefaultOccurrence> findByFirmIdAndActiveTrueAndStatusResolvedFalse(Long id);
}
