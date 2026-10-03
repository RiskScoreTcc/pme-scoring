package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.model.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByIdAndStatus(Long id, EntityStatus status);

    Page<User> findByStatus(EntityStatus status, Pageable pageable);

    boolean existsByEmailAndStatus(String email, EntityStatus status);

    boolean existsByEmailAndStatusIn(String email, Collection status);

    Optional<User> findByEmailAndStatus(String email, EntityStatus entityStatus);

    Optional<User> findByIdAndStatusIn(Long id, Collection status);
}