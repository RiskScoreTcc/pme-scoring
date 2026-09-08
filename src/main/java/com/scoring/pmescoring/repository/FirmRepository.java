package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.Firm;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CNPJ;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FirmRepository extends JpaRepository<Firm, Long> {
    Optional<Firm> findByIdAndActiveTrue(Long id);
    Page<Firm> findByActiveTrue(Pageable pageable);
    boolean existsByCnpjAndActiveTrue(String cnpj);
}
