package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.common.exception.BusinessException;
import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.common.util.PageableSanitizer;
import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.domain.WeightConfiguration;
import com.scoring.pmescoring.dto.request.weightconfiguration.UpdateWeightConfigurationRequest;
import com.scoring.pmescoring.dto.request.weightconfiguration.WeightConfigurationRequest;
import com.scoring.pmescoring.mapper.WeightConfigurationMapper;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.repository.UserRepository;
import com.scoring.pmescoring.repository.WeightConfigurationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeightConfigurationServiceImplTest {

    private static final Long USER_ID = 10L;

    @Mock private WeightConfigurationRepository weightConfigurationRepository;
    @Mock private WeightConfigurationMapper weightConfigurationMapper;
    @Mock private UserRepository userRepository;
    @Mock private PageableSanitizer pageableSanitizer;

    @InjectMocks
    private WeightConfigurationServiceImpl service;

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Rejeita limiar de baixo risco menor ou igual ao de médio risco")
        void shouldRejectInvalidThresholds() {
            WeightConfigurationRequest request = mock(WeightConfigurationRequest.class);
            when(request.lowRiskThreshold()).thenReturn(400);
            when(request.mediumRiskThreshold()).thenReturn(400);

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Low risk threshold must be greater");
            verifyNoInteractions(weightConfigurationRepository);
        }

        @Test
        @DisplayName("Inativa as configurações ativas e salva a nova como única ativa")
        void shouldDeactivatePreviousAndSaveNew() {
            WeightConfigurationRequest request = mock(WeightConfigurationRequest.class);
            when(request.updatedByUserId()).thenReturn(USER_ID);
            when(request.lowRiskThreshold()).thenReturn(700);
            when(request.mediumRiskThreshold()).thenReturn(400);
            when(request.revenueWeight()).thenReturn(new BigDecimal("0.4"));

            when(userRepository.findByIdAndStatus(USER_ID, EntityStatus.ACTIVE)).thenReturn(Optional.of(mock(User.class)));
            WeightConfiguration old1 = mock(WeightConfiguration.class);
            WeightConfiguration old2 = mock(WeightConfiguration.class);
            when(weightConfigurationRepository.findByStatus(EntityStatus.ACTIVE)).thenReturn(List.of(old1, old2));
            when(weightConfigurationRepository.save(any(WeightConfiguration.class))).thenAnswer(inv -> inv.getArgument(0));

            service.create(request);

            verify(old1).delete();
            verify(old2).delete();

            ArgumentCaptor<WeightConfiguration> captor = ArgumentCaptor.forClass(WeightConfiguration.class);
            verify(weightConfigurationRepository, times(3)).save(captor.capture());
            WeightConfiguration created = captor.getAllValues().get(2);
            assertThat(created.getLowRiskThreshold()).isEqualTo(700);
            assertThat(created.getMediumRiskThreshold()).isEqualTo(400);
            assertThat(created.getRevenueWeight()).isEqualByComparingTo("0.4");
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException quando o usuário responsável não existe")
        void shouldThrowWhenUserNotFound() {
            WeightConfigurationRequest request = mock(WeightConfigurationRequest.class);
            when(request.updatedByUserId()).thenReturn(USER_ID);
            when(request.lowRiskThreshold()).thenReturn(700);
            when(request.mediumRiskThreshold()).thenReturn(400);
            when(userRepository.findByIdAndStatus(USER_ID, EntityStatus.ACTIVE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(request)).isInstanceOf(ResourceNotFoundException.class);
            verify(weightConfigurationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        private WeightConfiguration givenExistingConfiguration() {
            WeightConfiguration existing = mock(WeightConfiguration.class);
            lenient().when(existing.getRevenueWeight()).thenReturn(new BigDecimal("0.4"));
            lenient().when(existing.getTimeWeight()).thenReturn(new BigDecimal("0.3"));
            lenient().when(existing.getDefaultWeight()).thenReturn(new BigDecimal("0.3"));
            lenient().when(existing.getMaxRevenueReference()).thenReturn(new BigDecimal("100000"));
            lenient().when(existing.getMaxTimeReferenceMonths()).thenReturn(120);
            lenient().when(existing.getLowRiskThreshold()).thenReturn(700);
            lenient().when(existing.getMediumRiskThreshold()).thenReturn(400);
            when(weightConfigurationRepository.findByIdAndStatus(1L, EntityStatus.ACTIVE)).thenReturn(Optional.of(existing));
            return existing;
        }

        private void givenOmittedIntegerFields(UpdateWeightConfigurationRequest request) {
            lenient().when(request.lowRiskThreshold()).thenReturn(null);
            lenient().when(request.mediumRiskThreshold()).thenReturn(null);
            lenient().when(request.maxTimeReferenceMonths()).thenReturn(null);
        }

        @Test
        @DisplayName("Mantém os valores atuais nos campos não enviados e cria uma nova versão")
        void shouldMergeNullFieldsWithExistingValues() {
            WeightConfiguration existing = givenExistingConfiguration();
            UpdateWeightConfigurationRequest request = mock(UpdateWeightConfigurationRequest.class);
            when(request.updatedByUserId()).thenReturn(USER_ID);
            when(request.revenueWeight()).thenReturn(new BigDecimal("0.5"));
            givenOmittedIntegerFields(request);
            when(userRepository.findByIdAndStatus(USER_ID, EntityStatus.ACTIVE)).thenReturn(Optional.of(mock(User.class)));

            service.update(1L, request);

            verify(existing).delete();
            ArgumentCaptor<WeightConfiguration> captor = ArgumentCaptor.forClass(WeightConfiguration.class);
            verify(weightConfigurationRepository, times(2)).save(captor.capture());
            WeightConfiguration newVersion = captor.getAllValues().get(1);

            assertThat(newVersion.getRevenueWeight()).isEqualByComparingTo("0.5");   
            assertThat(newVersion.getTimeWeight()).isEqualByComparingTo("0.3");      
            assertThat(newVersion.getLowRiskThreshold()).isEqualTo(700);             
            assertThat(newVersion.getMediumRiskThreshold()).isEqualTo(400);          
        }

        @Test
        @DisplayName("Valida os limiares já combinados com os valores existentes")
        void shouldValidateThresholdsAfterMerge() {
            WeightConfiguration existing = givenExistingConfiguration();
            UpdateWeightConfigurationRequest request = mock(UpdateWeightConfigurationRequest.class);
            givenOmittedIntegerFields(request);
            when(request.lowRiskThreshold()).thenReturn(300);

            assertThatThrownBy(() -> service.update(1L, request)).isInstanceOf(BusinessException.class);
            verify(existing, never()).delete();
            verify(weightConfigurationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Não permite excluir a única configuração ativa")
        void shouldNotDeleteOnlyActiveConfiguration() {
            WeightConfiguration config = mock(WeightConfiguration.class);
            when(weightConfigurationRepository.findByIdAndStatus(1L, EntityStatus.ACTIVE)).thenReturn(Optional.of(config));
            when(weightConfigurationRepository.countByStatus(EntityStatus.ACTIVE)).thenReturn(1L);

            assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(BusinessException.class);
            verify(config, never()).delete();
        }
    }

    @Test
    @DisplayName("findById lança ResourceNotFoundException para id inexistente")
    void findByIdShouldThrowWhenNotFound() {
        when(weightConfigurationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}