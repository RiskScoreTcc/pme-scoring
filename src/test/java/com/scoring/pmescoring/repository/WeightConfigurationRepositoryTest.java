package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.domain.WeightConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.scoring.pmescoring.model.EntityStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class WeightConfigurationRepositoryTest extends RepositoryTestBase {

    @Autowired
    private WeightConfigurationRepository weightConfigurationRepository;

    private User admin;

    @BeforeEach
    void setUp() {
        admin = persistUser("admin@riskscore.com", com.scoring.pmescoring.model.TypeUser.ADMIN, ACTIVE);
    }

    @Test
    @DisplayName("findFirstByStatusOrderByIdDesc devolve a configuração ativa mais recente, ignorando excluídas mais novas")
    void shouldReturnLatestActiveConfiguration() {
        persistConfiguration(admin, ACTIVE);
        WeightConfiguration latestActive = persistConfiguration(admin, ACTIVE);
        persistConfiguration(admin, DELETED); // mais nova, porém excluída

        assertThat(weightConfigurationRepository.findFirstByStatusOrderByIdDesc(ACTIVE)).contains(latestActive);
    }

    @Test
    @DisplayName("findFirstByStatusOrderByIdDesc devolve vazio quando não há configuração ativa")
    void shouldReturnEmptyWhenNoActiveConfiguration() {
        persistConfiguration(admin, DELETED);

        assertThat(weightConfigurationRepository.findFirstByStatusOrderByIdDesc(ACTIVE)).isEmpty();
    }

    @Test
    @DisplayName("findByStatus e countByStatus consideram só o status pedido")
    void findAndCountByStatus() {
        persistConfiguration(admin, ACTIVE);
        persistConfiguration(admin, DELETED);
        persistConfiguration(admin, DELETED);

        assertThat(weightConfigurationRepository.findByStatus(ACTIVE)).hasSize(1);
        assertThat(weightConfigurationRepository.countByStatus(ACTIVE)).isEqualTo(1);
        assertThat(weightConfigurationRepository.countByStatus(DELETED)).isEqualTo(2);
    }

    @Test
    @DisplayName("findByIdAndStatus não encontra configuração excluída ao buscar por ACTIVE")
    void findByIdAndStatusShouldRespectStatus() {
        WeightConfiguration deleted = persistConfiguration(admin, DELETED);

        assertThat(weightConfigurationRepository.findByIdAndStatus(deleted.getId(), ACTIVE)).isEmpty();
        assertThat(weightConfigurationRepository.findById(deleted.getId())).isPresent();
    }

    @Test
    @DisplayName("Os pesos são gravados e lidos com os valores corretos")
    void shouldPersistWeights() {
        WeightConfiguration configuration = persistConfiguration(admin, ACTIVE);
        em.clear();

        WeightConfiguration reloaded = weightConfigurationRepository.findById(configuration.getId()).orElseThrow();

        assertThat(reloaded.getRevenueWeight()).isEqualByComparingTo("0.40");
        assertThat(reloaded.getTimeWeight()).isEqualByComparingTo("0.30");
        assertThat(reloaded.getDefaultWeight()).isEqualByComparingTo("0.30");
        assertThat(reloaded.getLowRiskThreshold()).isEqualTo(700);
        assertThat(reloaded.getMediumRiskThreshold()).isEqualTo(400);
    }
}
