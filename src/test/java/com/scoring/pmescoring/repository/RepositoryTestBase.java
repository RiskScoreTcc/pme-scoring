package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.TestcontainersConfiguration;
import com.scoring.pmescoring.domain.*;
import com.scoring.pmescoring.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;

// Base dos testes de repository.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
abstract class RepositoryTestBase {

    @Autowired
    protected TestEntityManager em;

    protected User persistUser(String email, TypeUser type, EntityStatus status) {
        User user = new User(email, "$2a$10$senhaJaCriptografada", type);
        user.setStatus(status);
        return em.persistAndFlush(user);
    }

    protected User persistAnalyst(String email) {
        return persistUser(email, TypeUser.CREDIT_ANALYST, EntityStatus.ACTIVE);
    }

    protected Firm persistFirm(User owner, String cnpj, String name, EntityStatus status) {
        Firm firm = new Firm(owner, cnpj, name, new BigDecimal("50000.00"), 60, 10);
        firm.setStatus(status);
        return em.persistAndFlush(firm);
    }

    protected CalculatedScore persistScore(Firm firm, User analyst, int value, RiskBand band, EntityStatus status) {
        CalculatedScore score = new CalculatedScore(firm, analyst, value, band, "Justificativa do score " + value, factors());
        score.setStatus(status);
        return em.persistAndFlush(score);
    }

    protected DefaultOccurrence persistOccurrence(Firm firm, String amountDue, boolean resolved, EntityStatus status) {
        DefaultOccurrence occurrence = new DefaultOccurrence(firm, LocalDate.of(2026, 9, 1), new BigDecimal(amountDue), "Boleto em atraso");
        occurrence.setStatusResolved(resolved);
        occurrence.setStatus(status);
        return em.persistAndFlush(occurrence);
    }

    protected WeightConfiguration persistConfiguration(User user, EntityStatus status) {
        WeightConfiguration configuration = new WeightConfiguration(
                FormulaType.LINEAR_WEIGHTED_V1,
                new BigDecimal("0.40"),
                new BigDecimal("0.30"),
                new BigDecimal("0.30"),
                new BigDecimal("100000.00"),
                120,
                700,
                400,
                user
        );
        configuration.setStatus(status);
        return em.persistAndFlush(configuration);
    }

    protected static ScoreFactorsDTO factors() {
        return new ScoreFactorsDTO(
                LocalDate.of(2026, 10, 4),
                new BigDecimal("50000.00"),
                60,
                0,
                new BigDecimal("0.40"),
                new BigDecimal("0.30"),
                new BigDecimal("0.30"),
                false,
                BigDecimal.ZERO,
                new BigDecimal("100000.00"),
                120
        );
    }
}
