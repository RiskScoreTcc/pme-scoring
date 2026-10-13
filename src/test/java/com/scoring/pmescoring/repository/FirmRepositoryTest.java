package com.scoring.pmescoring.repository;

import com.scoring.pmescoring.domain.Firm;
import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.dto.response.firm.FirmRiskResponse;
import com.scoring.pmescoring.model.RiskBand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static com.scoring.pmescoring.model.EntityStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class FirmRepositoryTest extends RepositoryTestBase {

    @Autowired
    private FirmRepository firmRepository;

    private User analyst;

    @BeforeEach
    void setUp() {
        analyst = persistAnalyst("analista@riskscore.com");
    }

    @Nested
    @DisplayName("Consultas derivadas")
    class DerivedQueries {

        @Test
        @DisplayName("findByIdAndStatus não encontra empresa excluída ao buscar por ACTIVE")
        void findByIdAndStatusShouldRespectStatus() {
            Firm active = persistFirm(analyst, "11111111000101", "Alfa Ltda", ACTIVE);
            Firm deleted = persistFirm(analyst, "22222222000102", "Beta Ltda", DELETED);

            assertThat(firmRepository.findByIdAndStatus(active.getId(), ACTIVE)).contains(active);
            assertThat(firmRepository.findByIdAndStatus(deleted.getId(), ACTIVE)).isEmpty();
        }

        @Test
        @DisplayName("findByStatus pagina apenas as empresas com o status pedido")
        void findByStatusShouldPaginate() {
            persistFirm(analyst, "11111111000101", "Alfa Ltda", ACTIVE);
            persistFirm(analyst, "22222222000102", "Beta Ltda", ACTIVE);
            persistFirm(analyst, "33333333000103", "Gama Ltda", ACTIVE);
            persistFirm(analyst, "44444444000104", "Delta Ltda", DELETED);

            Page<Firm> firstPage = firmRepository.findByStatus(ACTIVE, PageRequest.of(0, 2));

            assertThat(firstPage.getContent()).hasSize(2);
            assertThat(firstPage.getTotalElements()).isEqualTo(3);
            assertThat(firstPage.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("existsByCnpjAndStatus: CNPJ de empresa excluída fica livre para novo cadastro")
        void existsByCnpjShouldIgnoreDeletedFirms() {
            persistFirm(analyst, "11111111000101", "Alfa Ltda", ACTIVE);
            persistFirm(analyst, "22222222000102", "Beta Ltda", DELETED);

            assertThat(firmRepository.existsByCnpjAndStatus("11111111000101", ACTIVE)).isTrue();
            assertThat(firmRepository.existsByCnpjAndStatus("22222222000102", ACTIVE)).isFalse();
            assertThat(firmRepository.existsByCnpjAndStatus("99999999000199", ACTIVE)).isFalse();
        }

        @Test
        @DisplayName("@PrePersist preenche a data de criação")
        void shouldFillCreationDate() {
            Firm firm = persistFirm(analyst, "11111111000101", "Alfa Ltda", ACTIVE);

            assertThat(firm.getCreationDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findActiveFirmsByRiskBand (consulta do dashboard)")
    class DashboardQuery {

        private Page<FirmRiskResponse> query(RiskBand riskBand, int page, int size) {
            return firmRepository.findActiveFirmsByRiskBand(ACTIVE, ACTIVE, riskBand, PageRequest.of(page, size));
        }

        @Test
        @DisplayName("Sem filtro (riskBand nulo) traz todas as empresas ativas com score ativo, montando o DTO")
        void nullRiskBandShouldReturnAll() {
            Firm alfa = persistFirm(analyst, "11111111000101", "Alfa Ltda", ACTIVE);
            Firm beta = persistFirm(analyst, "22222222000102", "Beta Ltda", ACTIVE);
            persistScore(alfa, analyst, 850, RiskBand.LOW, ACTIVE);
            persistScore(beta, analyst, 300, RiskBand.HIGH, ACTIVE);

            Page<FirmRiskResponse> result = query(null, 0, 10);

            assertThat(result.getContent()).containsExactlyInAnyOrder(
                    new FirmRiskResponse("11111111000101", "Alfa Ltda", 850, RiskBand.LOW, "Justificativa do score 850"),
                    new FirmRiskResponse("22222222000102", "Beta Ltda", 300, RiskBand.HIGH, "Justificativa do score 300")
            );
        }

        @Test
        @DisplayName("Com filtro, traz só as empresas da faixa pedida")
        void shouldFilterByRiskBand() {
            Firm alfa = persistFirm(analyst, "11111111000101", "Alfa Ltda", ACTIVE);
            Firm beta = persistFirm(analyst, "22222222000102", "Beta Ltda", ACTIVE);
            persistScore(alfa, analyst, 850, RiskBand.LOW, ACTIVE);
            persistScore(beta, analyst, 300, RiskBand.HIGH, ACTIVE);

            assertThat(query(RiskBand.HIGH, 0, 10).getContent())
                    .extracting(FirmRiskResponse::cnpj)
                    .containsExactly("22222222000102");
            assertThat(query(RiskBand.MEDIUM, 0, 10).getContent()).isEmpty();
        }

        @Test
        @DisplayName("Considera só o cálculo vigente: scores inativos (recálculos antigos) não aparecem nem duplicam a empresa")
        void shouldIgnoreInactiveScores() {
            Firm alfa = persistFirm(analyst, "11111111000101", "Alfa Ltda", ACTIVE);
            persistScore(alfa, analyst, 300, RiskBand.HIGH, INACTIVE);   // cálculo antigo
            persistScore(alfa, analyst, 850, RiskBand.LOW, ACTIVE);      // cálculo atual

            assertThat(query(null, 0, 10).getContent())
                    .extracting(FirmRiskResponse::scoreValue)
                    .containsExactly(850);
            assertThat(query(RiskBand.HIGH, 0, 10).getContent()).isEmpty();
        }

        @Test
        @DisplayName("Não lista empresas excluídas, mesmo que tenham score ativo")
        void shouldIgnoreDeletedFirms() {
            Firm deleted = persistFirm(analyst, "11111111000101", "Alfa Ltda", DELETED);
            persistScore(deleted, analyst, 850, RiskBand.LOW, ACTIVE);

            assertThat(query(null, 0, 10).getContent()).isEmpty();
        }

        @Test
        @DisplayName("Empresa sem nenhum score calculado não aparece no dashboard (JOIN interno)")
        void shouldIgnoreFirmsWithoutScore() {
            persistFirm(analyst, "11111111000101", "Alfa Ltda", ACTIVE);

            assertThat(query(null, 0, 10).getContent()).isEmpty();
        }

        @Test
        @DisplayName("Pagina corretamente, inclusive a contagem total usada pela exportação CSV")
        void shouldPaginate() {
            for (int i = 1; i <= 3; i++) {
                Firm firm = persistFirm(analyst, "1111111100010" + i, "Empresa " + i, ACTIVE);
                persistScore(firm, analyst, 500 + i, RiskBand.MEDIUM, ACTIVE);
            }

            Page<FirmRiskResponse> firstPage = query(null, 0, 2);
            Page<FirmRiskResponse> secondPage = query(null, 1, 2);

            assertThat(firstPage.getContent()).hasSize(2);
            assertThat(firstPage.getTotalElements()).isEqualTo(3);
            assertThat(firstPage.hasNext()).isTrue();
            assertThat(secondPage.getContent()).hasSize(1);
            assertThat(secondPage.hasNext()).isFalse();
        }
    }
}
