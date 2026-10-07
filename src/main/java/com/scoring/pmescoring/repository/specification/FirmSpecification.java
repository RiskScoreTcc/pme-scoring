package com.scoring.pmescoring.repository.specification;

import com.scoring.pmescoring.domain.CalculatedScore;
import com.scoring.pmescoring.domain.Firm;
import com.scoring.pmescoring.dto.request.firm.FirmFilter;
import com.scoring.pmescoring.model.EntityStatus;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class FirmSpecification {

    private FirmSpecification() {}

    public static Specification<Firm> filter(FirmFilter filter) {

        return (root, query, criteriaBuilder) -> {

            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("status"), EntityStatus.ACTIVE));

            if (StringUtils.hasText(filter.query())) {
                String cleanTerm = filter.query().trim();

                Predicate nameLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("registeredCompanyName")),
                        "%" + cleanTerm.toLowerCase() + "%"
                );

                Predicate cnpjLike = criteriaBuilder.like(
                        root.get("cnpj"),
                        "%" + cleanTerm + "%"
                );

                predicates.add(criteriaBuilder.or(nameLike, cnpjLike));
            }

            if (filter.riskBand() != null) {
                Subquery<Long> scoreSubquery = query.subquery(Long.class);
                Root<CalculatedScore> scoreRoot = scoreSubquery.from(CalculatedScore.class);

                scoreSubquery.select(scoreRoot.get("firm").get("id"))
                        .where(
                                criteriaBuilder.equal(scoreRoot.get("firm"), root),
                                criteriaBuilder.equal(scoreRoot.get("status"), EntityStatus.ACTIVE),
                                criteriaBuilder.equal(scoreRoot.get("riskBand"), filter.riskBand())
                        );

                predicates.add(criteriaBuilder.exists(scoreSubquery));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}