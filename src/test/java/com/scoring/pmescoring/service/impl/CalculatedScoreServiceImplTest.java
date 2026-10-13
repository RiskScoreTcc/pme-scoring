package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.common.exception.BusinessException;
import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.common.util.PageableSanitizer;
import com.scoring.pmescoring.domain.*;
import com.scoring.pmescoring.dto.request.calculatedscore.CalculatedScoreRequest;
import com.scoring.pmescoring.mapper.CalculatedScoreMapper;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.model.RiskBand;
import com.scoring.pmescoring.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do cálculo de score.
 *
 * Configuração padrão usada nos cenários:
 *  - Faturamento de referência máximo: R$ 100.000
 *  - Tempo de referência máximo: 120 meses
 *  - Pesos: faturamento 0,4 | tempo 0,3 | inadimplência 0,3
 *  - Faixas: >= 700 LOW | >= 400 MEDIUM | abaixo disso HIGH
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CalculatedScoreServiceImplTest {

    private static final Long FIRM_ID = 1L;
    private static final Long USER_ID = 10L;

    @Mock private CalculatedScoreRepository calculatedScoreRepository;
    @Mock private CalculatedScoreMapper calculatedScoreMapper;
    @Mock private FirmRepository firmRepository;
    @Mock private UserRepository userRepository;
    @Mock private WeightConfigurationRepository weightConfigurationRepository;
    @Mock private DefaultOccurrenceRepository defaultOccurrenceRepository;
    @Mock private PageableSanitizer pageableSanitizer;

    @InjectMocks
    private CalculatedScoreServiceImpl service;

    private Firm firm;
    private User user;
    private WeightConfiguration config;
    private CalculatedScoreRequest request;

    @BeforeEach
    void setUp() {
        firm = mock(Firm.class);
        user = mock(User.class);
        config = mock(WeightConfiguration.class);
        request = mock(CalculatedScoreRequest.class);

        when(request.firmId()).thenReturn(FIRM_ID);
        when(request.userId()).thenReturn(USER_ID);

        when(calculatedScoreRepository.findByFirmIdAndStatus(FIRM_ID, EntityStatus.ACTIVE)).thenReturn(List.of());
        when(firmRepository.findByIdAndStatus(FIRM_ID, EntityStatus.ACTIVE)).thenReturn(Optional.of(firm));
        when(userRepository.findByIdAndStatus(USER_ID, EntityStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(weightConfigurationRepository.findFirstByStatusOrderByIdDesc(EntityStatus.ACTIVE)).thenReturn(Optional.of(config));
        when(defaultOccurrenceRepository.findByFirmIdAndStatusAndStatusResolvedFalse(FIRM_ID, EntityStatus.ACTIVE))
                .thenReturn(List.of());

        givenWeights("0.4", "0.3", "0.3");
        when(config.getMaxRevenueReference()).thenReturn(new BigDecimal("100000"));
        when(config.getMaxTimeReferenceMonths()).thenReturn(120);
        when(config.getLowRiskThreshold()).thenReturn(700);
        when(config.getMediumRiskThreshold()).thenReturn(400);
    }

    private void givenWeights(String revenue, String time, String defaults) {
        when(config.getRevenueWeight()).thenReturn(new BigDecimal(revenue));
        when(config.getTimeWeight()).thenReturn(new BigDecimal(time));
        when(config.getDefaultWeight()).thenReturn(new BigDecimal(defaults));
    }

    private void givenFirm(String averageRevenue, int timeMonths) {
        when(firm.getAverageRevenue()).thenReturn(new BigDecimal(averageRevenue));
        when(firm.getTimeMonths()).thenReturn(timeMonths);
    }

    private CalculatedScore captureLastSavedScore() {
        ArgumentCaptor<CalculatedScore> captor = ArgumentCaptor.forClass(CalculatedScore.class);
        verify(calculatedScoreRepository, atLeastOnce()).save(captor.capture());
        List<CalculatedScore> saved = captor.getAllValues();
        return saved.get(saved.size() - 1);
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Calcula 650 pontos (MEDIUM) para empresa sem inadimplência e na metade das referências")
        void shouldCalculateMediumRiskScore() {
            givenFirm("50000", 60);

            service.create(request);

            CalculatedScore saved = captureLastSavedScore();
            assertThat(saved.getScoreValue()).isEqualTo(650);
            assertThat(saved.getRiskBand()).isEqualTo(RiskBand.MEDIUM);
            verify(calculatedScoreMapper).toResponse(saved);
        }

        @Test
        @DisplayName("Inadimplência ativa zera o fator de pagamento e derruba a empresa para HIGH")
        void shouldZeroDefaultFactorWhenThereAreActiveDefaults() {
            givenFirm("50000", 60);
            DefaultOccurrence occurrence = mock(DefaultOccurrence.class);
            when(occurrence.getAmountDue()).thenReturn(new BigDecimal("1500.00"));
            when(defaultOccurrenceRepository.findByFirmIdAndStatusAndStatusResolvedFalse(FIRM_ID, EntityStatus.ACTIVE))
                    .thenReturn(List.of(occurrence));

            service.create(request);

            CalculatedScore saved = captureLastSavedScore();
            assertThat(saved.getScoreValue()).isEqualTo(350);
            assertThat(saved.getRiskBand()).isEqualTo(RiskBand.HIGH);
            assertThat(saved.getJustification()).contains("active default occurrences");
        }

        @Test
        @DisplayName("Faturamento e tempo acima da referência são limitados ao teto (score máximo 1000)")
        void shouldCapRevenueAndTimeAtReference() {
            givenFirm("999999", 500);

            service.create(request);

            CalculatedScore saved = captureLastSavedScore();
            assertThat(saved.getScoreValue()).isEqualTo(1000);
            assertThat(saved.getRiskBand()).isEqualTo(RiskBand.LOW);
        }

        @ParameterizedTest(name = "faturamento {0} → score {1} → {2}")
        @DisplayName("Classifica corretamente nos limites das faixas de risco")
        @CsvSource({
                "70000, 700, LOW",
                "69900, 699, MEDIUM",
                "40000, 400, MEDIUM",
                "39900, 399, HIGH"
        })
        void shouldClassifyRiskBandAtThresholds(String revenue, int expectedScore, RiskBand expectedBand) {
            givenWeights("1", "0", "0");
            givenFirm(revenue, 0);

            service.create(request);

            CalculatedScore saved = captureLastSavedScore();
            assertThat(saved.getScoreValue()).isEqualTo(expectedScore);
            assertThat(saved.getRiskBand()).isEqualTo(expectedBand);
        }

        @Test
        @DisplayName("Inativa os cálculos ativos anteriores da mesma empresa antes de salvar o novo")
        void shouldDeactivatePreviousScores() {
            givenFirm("50000", 60);
            CalculatedScore previous = mock(CalculatedScore.class);
            when(calculatedScoreRepository.findByFirmIdAndStatus(FIRM_ID, EntityStatus.ACTIVE))
                    .thenReturn(List.of(previous));

            service.create(request);

            verify(previous).inactive();
            verify(calculatedScoreRepository).save(previous);
            verify(calculatedScoreRepository, times(2)).save(any(CalculatedScore.class));
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException quando a empresa não existe ou está inativa")
        void shouldThrowWhenFirmNotFound() {
            when(firmRepository.findByIdAndStatus(FIRM_ID, EntityStatus.ACTIVE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Firm not found");
            verify(calculatedScoreRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException quando o usuário não existe ou está inativo")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByIdAndStatus(USER_ID, EntityStatus.ACTIVE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
            verify(calculatedScoreRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança BusinessException quando não há configuração de pesos ativa")
        void shouldThrowWhenNoActiveWeightConfiguration() {
            when(weightConfigurationRepository.findFirstByStatusOrderByIdDesc(EntityStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No active weight configuration");
            verify(calculatedScoreRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Não permite excluir o único score ativo do sistema")
        void shouldNotDeleteOnlyActiveScore() {
            CalculatedScore score = mock(CalculatedScore.class);
            when(calculatedScoreRepository.findByIdAndStatus(5L, EntityStatus.ACTIVE)).thenReturn(Optional.of(score));
            when(calculatedScoreRepository.countByStatus(EntityStatus.ACTIVE)).thenReturn(1L);

            assertThatThrownBy(() -> service.delete(5L)).isInstanceOf(BusinessException.class);
            verify(score, never()).inactive();
        }

        @Test
        @DisplayName("Inativa o score quando há outros ativos")
        void shouldInactivateScore() {
            CalculatedScore score = mock(CalculatedScore.class);
            when(calculatedScoreRepository.findByIdAndStatus(5L, EntityStatus.ACTIVE)).thenReturn(Optional.of(score));
            when(calculatedScoreRepository.countByStatus(EntityStatus.ACTIVE)).thenReturn(3L);

            service.delete(5L);

            verify(score).inactive();
            verify(calculatedScoreRepository).save(score);
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException para score inexistente")
        void shouldThrowWhenScoreNotFound() {
            when(calculatedScoreRepository.findByIdAndStatus(99L, EntityStatus.ACTIVE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
