package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.common.exception.BusinessException;
import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.common.util.PageableSanitizer;
import com.scoring.pmescoring.domain.CalculatedScore;
import com.scoring.pmescoring.domain.DefaultOccurrence;
import com.scoring.pmescoring.domain.Firm;
import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.dto.request.firm.FirmRequest;
import com.scoring.pmescoring.dto.request.firm.UpdateFirmRequest;
import com.scoring.pmescoring.dto.response.firm.FirmResponse;
import com.scoring.pmescoring.mapper.FirmMapper;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.repository.CalculatedScoreRepository;
import com.scoring.pmescoring.repository.DefaultOccurrenceRepository;
import com.scoring.pmescoring.repository.FirmRepository;
import com.scoring.pmescoring.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirmServiceImplTest {

    private static final Long USER_ID = 10L;
    private static final Long FIRM_ID = 1L;
    private static final String CNPJ = "12345678000199";

    @Mock private FirmRepository firmRepository;
    @Mock private FirmMapper firmMapper;
    @Mock private UserRepository userRepository;
    @Mock private CalculatedScoreRepository calculatedScoreRepository;
    @Mock private DefaultOccurrenceRepository defaultOccurrenceRepository;
    @Mock private PageableSanitizer pageableSanitizer;

    @InjectMocks
    private FirmServiceImpl service;

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Rejeita CNPJ já cadastrado em empresa ativa")
        void shouldRejectDuplicateCnpj() {
            FirmRequest request = mock(FirmRequest.class);
            when(request.cnpj()).thenReturn(CNPJ);
            when(firmRepository.existsByCnpjAndStatus(CNPJ, EntityStatus.ACTIVE)).thenReturn(true);

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already exists");
            verify(firmRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException quando o usuário não existe")
        void shouldThrowWhenUserNotFound() {
            FirmRequest request = mock(FirmRequest.class);
            when(request.cnpj()).thenReturn(CNPJ);
            when(request.userId()).thenReturn(USER_ID);
            when(firmRepository.existsByCnpjAndStatus(CNPJ, EntityStatus.ACTIVE)).thenReturn(false);
            when(userRepository.findByIdAndStatus(USER_ID, EntityStatus.ACTIVE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(request)).isInstanceOf(ResourceNotFoundException.class);
            verify(firmRepository, never()).save(any());
        }

        @Test
        @DisplayName("Salva a empresa e devolve a resposta mapeada")
        void shouldCreateFirm() {
            FirmRequest request = mock(FirmRequest.class);
            when(request.cnpj()).thenReturn(CNPJ);
            when(request.userId()).thenReturn(USER_ID);
            when(firmRepository.existsByCnpjAndStatus(CNPJ, EntityStatus.ACTIVE)).thenReturn(false);
            when(userRepository.findByIdAndStatus(USER_ID, EntityStatus.ACTIVE)).thenReturn(Optional.of(mock(User.class)));

            Firm saved = mock(Firm.class);
            FirmResponse response = mock(FirmResponse.class);
            when(firmRepository.save(any(Firm.class))).thenReturn(saved);
            when(firmMapper.toResponse(saved)).thenReturn(response);

            FirmResponse result = service.create(request);

            assertThat(result).isSameAs(response);
            verify(firmRepository).save(any(Firm.class));
        }
    }

    @Test
    @DisplayName("delete() exclui a empresa em cascata com seus scores e ocorrências de inadimplência")
    void deleteShouldCascadeToScoresAndOccurrences() {
        Firm firm = mock(Firm.class);
        CalculatedScore score1 = mock(CalculatedScore.class);
        CalculatedScore score2 = mock(CalculatedScore.class);
        DefaultOccurrence occurrence = mock(DefaultOccurrence.class);

        when(firmRepository.findByIdAndStatus(FIRM_ID, EntityStatus.ACTIVE)).thenReturn(Optional.of(firm));
        when(calculatedScoreRepository.findByFirmIdAndStatus(FIRM_ID, EntityStatus.ACTIVE)).thenReturn(List.of(score1, score2));
        when(defaultOccurrenceRepository.findByFirmIdAndStatus(FIRM_ID, EntityStatus.ACTIVE)).thenReturn(List.of(occurrence));

        service.delete(FIRM_ID);

        verify(firm).delete();
        verify(score1).delete();
        verify(score2).delete();
        verify(occurrence).delete();
        verify(calculatedScoreRepository, times(2)).save(any());
        verify(defaultOccurrenceRepository).save(occurrence);
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Rejeita troca para um CNPJ já usado por outra empresa")
        void shouldRejectCnpjInUse() {
            Firm firm = mock(Firm.class);
            when(firm.getCnpj()).thenReturn(CNPJ);
            when(firmRepository.findByIdAndStatus(FIRM_ID, EntityStatus.ACTIVE)).thenReturn(Optional.of(firm));

            UpdateFirmRequest request = mock(UpdateFirmRequest.class);
            when(request.cnpj()).thenReturn("98765432000111");
            when(firmRepository.existsByCnpjAndStatus("98765432000111", EntityStatus.ACTIVE)).thenReturn(true);

            assertThatThrownBy(() -> service.update(FIRM_ID, request)).isInstanceOf(BusinessException.class);
            verify(firmRepository, never()).save(any());
        }

        @Test
        @DisplayName("Mantendo o mesmo CNPJ, não consulta duplicidade e salva")
        void shouldNotCheckDuplicityWhenCnpjUnchanged() {
            Firm firm = mock(Firm.class);
            when(firm.getCnpj()).thenReturn(CNPJ);
            when(firmRepository.findByIdAndStatus(FIRM_ID, EntityStatus.ACTIVE)).thenReturn(Optional.of(firm));

            UpdateFirmRequest request = mock(UpdateFirmRequest.class);
            when(request.cnpj()).thenReturn(CNPJ);

            service.update(FIRM_ID, request);

            verify(firmRepository, never()).existsByCnpjAndStatus(anyString(), any());
            verify(firmRepository).save(firm);
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException para empresa inexistente")
        void shouldThrowWhenFirmNotFound() {
            when(firmRepository.findByIdAndStatus(FIRM_ID, EntityStatus.ACTIVE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(FIRM_ID, mock(UpdateFirmRequest.class)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
