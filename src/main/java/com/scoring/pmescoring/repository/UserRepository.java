package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.dto.response.user.UserMetricsResponse;
import com.scoring.pmescoring.model.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByIdAndStatus(Long id, EntityStatus status);

    Page<User> findByStatus(EntityStatus status, Pageable pageable);

    boolean existsByEmailAndStatus(String email, EntityStatus status);

    boolean existsByEmailAndStatusIn(String email, Collection status);

    Optional<User> findByEmailAndStatus(String email, EntityStatus entityStatus);

    Optional<User> findByIdAndStatusIn(Long id, Collection status);

    @Query("""
        SELECT new com.scoring.pmescoring.dto.response.user.UserMetricsResponse(
            COUNT(u),
            COUNT(CASE WHEN u.status = com.scoring.pmescoring.model.EntityStatus.ACTIVE THEN 1 END),
            COUNT(CASE WHEN u.userType = com.scoring.pmescoring.model.TypeUser.ADMIN THEN 1 END),
            COUNT(CASE WHEN u.userType = com.scoring.pmescoring.model.TypeUser.CREDIT_ANALYST THEN 1 END)
        )
        FROM User u
    """)
    UserMetricsResponse getUserMetrics();
}