package com.scoring.pmescoring.repository.specification;

import com.scoring.pmescoring.domain.DefaultOccurrence;
import com.scoring.pmescoring.domain.Firm;
import com.scoring.pmescoring.dto.request.defaultoccurrence.DefaultOccurrenceFilter;
import com.scoring.pmescoring.model.EntityStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultOccurrenceSpecification {

    private DefaultOccurrenceSpecification() {}

    public static Specification<DefaultOccurrence> filter(DefaultOccurrenceFilter filter) {

        return (root, query, criteriaBuilder) -> {

            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.statusResolved() != null) {
                predicates.add(criteriaBuilder.equal(root.get("statusResolved"), filter.statusResolved()));
            }

            Join<DefaultOccurrence, Firm> firm = root.join("firm", JoinType.INNER);

            predicates.add(criteriaBuilder.equal(firm.get("status"), EntityStatus.ACTIVE));

            if (StringUtils.hasText(filter.query())) {
                String cleanTerm = filter.query().trim();

                Predicate nameLike = criteriaBuilder.like(
                        criteriaBuilder.lower(firm.get("registeredCompanyName")),
                        "%" + cleanTerm.toLowerCase() + "%"
                );

                Predicate cnpjLike = criteriaBuilder.like(
                        firm.get("cnpj"),
                        "%" + cleanTerm + "%"
                );

                predicates.add(criteriaBuilder.or(nameLike, cnpjLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}