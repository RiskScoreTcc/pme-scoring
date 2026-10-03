package com.scoring.pmescoring.repository.specification;

import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.dto.request.user.UserFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> filter(UserFilter filter) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.id() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), filter.id()));
            }

            if (filter.email() != null && !filter.email().isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + filter.email().toLowerCase() + "%"));
            }

            if (filter.type() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userType"), filter.type()));
            }

            if (filter.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.status()));
            }

            if (filter.lastAccessFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("lastAccess"), filter.lastAccessFrom().atStartOfDay()));
            }

            if (filter.lastAccessTo() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("lastAccess"), filter.lastAccessTo().plusDays(1).atStartOfDay()));
            }

            if (filter.creationDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("creationDate"), filter.creationDateFrom()));
            }

            if (filter.creationDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("creationDate"), filter.creationDateTo()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}