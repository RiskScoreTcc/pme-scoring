package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.DefaultOccurrence;
import com.scoring.pmescoring.domain.Firm;
import com.scoring.pmescoring.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static com.scoring.pmescoring.model.EntityStatus.ACTIVE;
import static com.scoring.pmescoring.model.EntityStatus.DELETED;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultOccurrenceRepositoryTest extends RepositoryTestBase {

    @Autowired
    private DefaultOccurrenceRepository defaultOccurrenceRepository;

    private Firm alfa;
    private Firm beta;

    @BeforeEach
    void setUp() {
        User analyst = persistAnalyst("analista@riskscore.com");
        alfa = persistFirm(analyst, "11111111000101", "Alfa Ltda", ACTIVE);
        beta = persistFirm(analyst, "22222222000102", "Beta Ltda", ACTIVE);
    }

    @Test
    @DisplayName("Consulta usada no cálculo do score: só ocorrências ativas, não resolvidas e da própria empresa")
    void findUnresolvedShouldReturnOnlyOpenDefaultsOfTheFirm() {
        DefaultOccurrence open = persistOccurrence(alfa, "1500.00", false, ACTIVE);
        persistOccurrence(alfa, "800.00", true, ACTIVE);       // já resolvida
        persistOccurrence(alfa, "300.00", false, DELETED);     // lançada por engano e excluída
        persistOccurrence(beta, "999.00", false, ACTIVE);      // de outra empresa

        List<DefaultOccurrence> result =
                defaultOccurrenceRepository.findByFirmIdAndStatusAndStatusResolvedFalse(alfa.getId(), ACTIVE);

        assertThat(result).containsExactly(open);
    }

    @Test
    @DisplayName("Empresa sem inadimplência em aberto devolve lista vazia")
    void findUnresolvedShouldReturnEmptyWhenAllResolved() {
        persistOccurrence(alfa, "800.00", true, ACTIVE);

        assertThat(defaultOccurrenceRepository.findByFirmIdAndStatusAndStatusResolvedFalse(alfa.getId(), ACTIVE))
                .isEmpty();
    }

    @Test
    @DisplayName("findByFirmIdAndStatus (usada na exclusão em cascata) inclui as resolvidas, mas não as excluídas")
    void findByFirmIdAndStatusShouldIncludeResolved() {
        persistOccurrence(alfa, "1500.00", false, ACTIVE);
        persistOccurrence(alfa, "800.00", true, ACTIVE);
        persistOccurrence(alfa, "300.00", false, DELETED);

        assertThat(defaultOccurrenceRepository.findByFirmIdAndStatus(alfa.getId(), ACTIVE)).hasSize(2);
    }

    @Test
    @DisplayName("findByStatus pagina só as ocorrências ativas")
    void findByStatusShouldPaginate() {
        persistOccurrence(alfa, "100.00", false, ACTIVE);
        persistOccurrence(alfa, "200.00", false, ACTIVE);
        persistOccurrence(beta, "300.00", false, ACTIVE);
        persistOccurrence(beta, "400.00", false, DELETED);

        Page<DefaultOccurrence> firstPage = defaultOccurrenceRepository.findByStatus(ACTIVE, PageRequest.of(0, 2));

        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("O valor devido é gravado com 2 casas decimais (coluna numeric(12,2) arredonda)")
    void amountDueShouldBeStoredWithTwoDecimals() {
        DefaultOccurrence occurrence = persistOccurrence(alfa, "1500.555", false, ACTIVE);
        em.clear();

        DefaultOccurrence reloaded = em.find(DefaultOccurrence.class, occurrence.getId());

        assertThat(reloaded.getAmountDue()).isEqualByComparingTo("1500.56");
    }

    @Test
    @DisplayName("@PrePersist preenche a data de criação")
    void shouldFillCreationDate() {
        DefaultOccurrence occurrence = persistOccurrence(alfa, "1500.00", false, ACTIVE);

        assertThat(occurrence.getCreationDate()).isNotNull();
    }
}
