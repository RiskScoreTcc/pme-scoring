package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.CalculatedScore;
import com.scoring.pmescoring.domain.Firm;
import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.model.RiskBand;
import com.scoring.pmescoring.model.ScoreFactorsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static com.scoring.pmescoring.model.EntityStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class CalculatedScoreRepositoryTest extends RepositoryTestBase {

    @Autowired
    private CalculatedScoreRepository calculatedScoreRepository;

    private User analyst;
    private Firm alfa;
    private Firm beta;

    @BeforeEach
    void setUp() {
        analyst = persistAnalyst("analista@riskscore.com");
        alfa = persistFirm(analyst, "11111111000101", "Alfa Ltda", ACTIVE);
        beta = persistFirm(analyst, "22222222000102", "Beta Ltda", ACTIVE);
    }

    @Test
    @DisplayName("Grava e lê de volta os fatores do cálculo na coluna jsonb do PostgreSQL")
    void shouldRoundTripFactorsAsJsonb() {
        CalculatedScore score = persistScore(alfa, analyst, 650, RiskBand.MEDIUM, ACTIVE);
        em.clear();

        CalculatedScore reloaded = calculatedScoreRepository.findByIdAndStatus(score.getId(), ACTIVE).orElseThrow();
        ScoreFactorsDTO factors = reloaded.getFactorsJson();

        assertThat(factors.calculationDate()).isEqualTo(LocalDate.of(2026, 10, 4));
        assertThat(factors.averageRevenue()).isEqualByComparingTo("50000.00");
        assertThat(factors.timeMonths()).isEqualTo(60);
        assertThat(factors.revenueWeight()).isEqualByComparingTo("0.40");
        assertThat(factors.inDefault()).isFalse();
        assertThat(factors.maxTimeReferenceMonths()).isEqualTo(120);
    }

    @Test
    @DisplayName("@PrePersist preenche a data do cálculo")
    void shouldFillCalculationDate() {
        CalculatedScore score = persistScore(alfa, analyst, 650, RiskBand.MEDIUM, ACTIVE);

        assertThat(score.getCalculationDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("findByIdAndStatus não encontra score inativo ao buscar por ACTIVE")
    void findByIdAndStatusShouldRespectStatus() {
        CalculatedScore old = persistScore(alfa, analyst, 300, RiskBand.HIGH, INACTIVE);

        assertThat(calculatedScoreRepository.findByIdAndStatus(old.getId(), ACTIVE)).isEmpty();
        assertThat(calculatedScoreRepository.findByIdAndStatus(old.getId(), INACTIVE)).isPresent();
    }

    @Test
    @DisplayName("findByFirmIdAndStatus traz só os scores daquela empresa com o status pedido")
    void findByFirmIdAndStatusShouldFilterFirmAndStatus() {
        CalculatedScore alfaCurrent = persistScore(alfa, analyst, 850, RiskBand.LOW, ACTIVE);
        persistScore(alfa, analyst, 300, RiskBand.HIGH, INACTIVE);
        persistScore(beta, analyst, 500, RiskBand.MEDIUM, ACTIVE);

        List<CalculatedScore> result = calculatedScoreRepository.findByFirmIdAndStatus(alfa.getId(), ACTIVE);

        assertThat(result).containsExactly(alfaCurrent);
    }

    @Test
    @DisplayName("countByStatus conta os scores de todas as empresas")
    void countByStatusShouldCountAllFirms() {
        persistScore(alfa, analyst, 850, RiskBand.LOW, ACTIVE);
        persistScore(alfa, analyst, 300, RiskBand.HIGH, INACTIVE);
        persistScore(beta, analyst, 500, RiskBand.MEDIUM, ACTIVE);

        assertThat(calculatedScoreRepository.countByStatus(ACTIVE)).isEqualTo(2);
        assertThat(calculatedScoreRepository.countByStatus(INACTIVE)).isEqualTo(1);
    }

    @Test
    @DisplayName("findByStatusNot(DELETED) lista ativos e inativos (histórico), mas não excluídos")
    void findByStatusNotShouldExcludeOnlyDeleted() {
        persistScore(alfa, analyst, 850, RiskBand.LOW, ACTIVE);
        persistScore(alfa, analyst, 300, RiskBand.HIGH, INACTIVE);
        persistScore(beta, analyst, 500, RiskBand.MEDIUM, DELETED);

        Page<CalculatedScore> result = calculatedScoreRepository.findByStatusNot(DELETED, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(CalculatedScore::getStatus)
                .containsExactlyInAnyOrder(ACTIVE, INACTIVE);
    }
}
