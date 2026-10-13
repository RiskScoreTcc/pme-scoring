package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.common.util.PageableSanitizer;
import com.scoring.pmescoring.domain.DefaultOccurrence;
import com.scoring.pmescoring.domain.Firm;
import com.scoring.pmescoring.dto.request.defaultoccurrence.DefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.request.defaultoccurrence.UpdateDefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.response.defaultoccurrence.DefaultOccurrenceResponse;
import com.scoring.pmescoring.mapper.DefaultOccurrenceMapper;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.repository.DefaultOccurrenceRepository;
import com.scoring.pmescoring.repository.FirmRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultOccurrenceServiceImplTest {

    private static final Long FIRM_ID = 1L;
    private static final Long OCCURRENCE_ID = 7L;

    @Mock private DefaultOccurrenceRepository defaultOccurrenceRepository;
    @Mock private FirmRepository firmRepository;
    @Mock private DefaultOccurrenceMapper defaultOccurrenceMapper;
    @Mock private PageableSanitizer pageableSanitizer;

    @InjectMocks
    private DefaultOccurrenceServiceImpl service;

    private DefaultOccurrence givenActiveOccurrence() {
        DefaultOccurrence occurrence = mock(DefaultOccurrence.class);
        when(defaultOccurrenceRepository.findByIdAndStatus(OCCURRENCE_ID, EntityStatus.ACTIVE))
                .thenReturn(Optional.of(occurrence));
        return occurrence;
    }

    private void givenOccurrenceNotFound() {
        when(defaultOccurrenceRepository.findByIdAndStatus(OCCURRENCE_ID, EntityStatus.ACTIVE))
                .thenReturn(Optional.empty());
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Lança ResourceNotFoundException quando a empresa não existe ou está inativa")
        void shouldThrowWhenFirmNotFound() {
            DefaultOccurrenceRequest request = mock(DefaultOccurrenceRequest.class);
            when(request.firmId()).thenReturn(FIRM_ID);
            when(firmRepository.findByIdAndStatus(FIRM_ID, EntityStatus.ACTIVE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Firm not found");
            verify(defaultOccurrenceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Salva a ocorrência vinculada à empresa com o valor devido informado")
        void shouldCreateOccurrence() {
            DefaultOccurrenceRequest request = mock(DefaultOccurrenceRequest.class);
            when(request.firmId()).thenReturn(FIRM_ID);
            when(request.amountDue()).thenReturn(new BigDecimal("1500.00"));
            when(firmRepository.findByIdAndStatus(FIRM_ID, EntityStatus.ACTIVE)).thenReturn(Optional.of(mock(Firm.class)));
            DefaultOccurrenceResponse response = mock(DefaultOccurrenceResponse.class);
            when(defaultOccurrenceMapper.toResponse(any(DefaultOccurrence.class))).thenReturn(response);

            DefaultOccurrenceResponse result = service.create(request);

            ArgumentCaptor<DefaultOccurrence> captor = ArgumentCaptor.forClass(DefaultOccurrence.class);
            verify(defaultOccurrenceRepository).save(captor.capture());
            assertThat(captor.getValue().getAmountDue()).isEqualByComparingTo("1500.00");
            verify(defaultOccurrenceMapper).toResponse(captor.getValue());
            assertThat(result).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Exclui logicamente a ocorrência")
        void shouldDeleteOccurrence() {
            DefaultOccurrence occurrence = givenActiveOccurrence();

            service.delete(OCCURRENCE_ID);

            verify(occurrence).delete();
            verify(defaultOccurrenceRepository).save(occurrence);
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException para ocorrência inexistente")
        void shouldThrowWhenNotFound() {
            givenOccurrenceNotFound();

            assertThatThrownBy(() -> service.delete(OCCURRENCE_ID)).isInstanceOf(ResourceNotFoundException.class);
            verify(defaultOccurrenceRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById() / findAll()")
    class Find {

        @Test
        @DisplayName("findById devolve a resposta mapeada")
        void findByIdShouldReturnMappedResponse() {
            DefaultOccurrence occurrence = givenActiveOccurrence();
            DefaultOccurrenceResponse response = mock(DefaultOccurrenceResponse.class);
            when(defaultOccurrenceMapper.toResponse(occurrence)).thenReturn(response);

            assertThat(service.findById(OCCURRENCE_ID)).isSameAs(response);
        }

        @Test
        @DisplayName("findById lança ResourceNotFoundException para ocorrência inexistente")
        void findByIdShouldThrowWhenNotFound() {
            givenOccurrenceNotFound();

            assertThatThrownBy(() -> service.findById(OCCURRENCE_ID)).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("findAll sanitiza o Pageable e lista apenas ocorrências ativas")
        void findAllShouldSanitizeAndFilterActive() {
            Pageable requested = PageRequest.of(0, 5000);
            Pageable sanitized = PageRequest.of(0, 50);
            DefaultOccurrence occurrence = mock(DefaultOccurrence.class);
            DefaultOccurrenceResponse response = mock(DefaultOccurrenceResponse.class);

            when(pageableSanitizer.sanitize(requested)).thenReturn(sanitized);
            when(defaultOccurrenceRepository.findByStatus(EntityStatus.ACTIVE, sanitized))
                    .thenReturn(new PageImpl<>(List.of(occurrence)));
            when(defaultOccurrenceMapper.toResponse(occurrence)).thenReturn(response);

            Page<DefaultOccurrenceResponse> result = service.findAll(requested);

            assertThat(result.getContent()).containsExactly(response);
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Repassa os novos dados para a entidade e salva")
        void shouldUpdateOccurrence() {
            DefaultOccurrence occurrence = givenActiveOccurrence();
            UpdateDefaultOccurrenceRequest request = mock(UpdateDefaultOccurrenceRequest.class);
            when(request.amountDue()).thenReturn(new BigDecimal("2000.00"));
            when(request.description()).thenReturn("Boleto renegociado");

            // lido antes do verify: chamar outro mock dentro do verify confunde o Mockito
            var dateOccurrence = request.dateOccurrence();

            service.update(OCCURRENCE_ID, request);

            verify(occurrence).update(new BigDecimal("2000.00"), dateOccurrence, "Boleto renegociado");
            verify(defaultOccurrenceRepository).save(occurrence);
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException para ocorrência inexistente")
        void shouldThrowWhenNotFound() {
            givenOccurrenceNotFound();

            assertThatThrownBy(() -> service.update(OCCURRENCE_ID, mock(UpdateDefaultOccurrenceRequest.class)))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(defaultOccurrenceRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("Marca a ocorrência como resolvida (nunca reabre)")
        void shouldMarkAsResolved() {
            DefaultOccurrence occurrence = givenActiveOccurrence();

            service.updateStatus(OCCURRENCE_ID);

            verify(occurrence).setStatusResolved(true);
            verify(occurrence, never()).setStatusResolved(false);
            verify(defaultOccurrenceRepository).save(occurrence);
        }

        @Test
        @DisplayName("Lança ResourceNotFoundException para ocorrência inexistente")
        void shouldThrowWhenNotFound() {
            givenOccurrenceNotFound();

            assertThatThrownBy(() -> service.updateStatus(OCCURRENCE_ID)).isInstanceOf(ResourceNotFoundException.class);
            verify(defaultOccurrenceRepository, never()).save(any());
        }
    }
}
