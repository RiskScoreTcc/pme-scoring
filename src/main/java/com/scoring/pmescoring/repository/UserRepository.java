package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdAndActiveTrue(Long id);
    Page<User> findByActiveTrue(Pageable pageable);
    boolean existsByIdAndActiveTrue(Long id);
    boolean existsByEmailAndActiveTrue(String email);
}
