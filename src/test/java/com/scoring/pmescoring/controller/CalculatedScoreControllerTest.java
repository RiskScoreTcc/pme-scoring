package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.common.exception.BusinessException;
import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.dto.request.calculatedscore.CalculatedScoreRequest;
import com.scoring.pmescoring.dto.request.calculatedscore.UpdateCalculatedScoreRequest;
import com.scoring.pmescoring.dto.response.calculatedscore.CalculatedScoreResponse;
import com.scoring.pmescoring.service.CalculatedScoreService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CalculatedScoreControllerTest {

    private static final String BASE_URL = "/api/v1/calculated-scores";

    @Mock
    private CalculatedScoreService calculatedScoreService;

    @InjectMocks
    private CalculatedScoreController controller;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = ControllerTestSupport.standalone(controller);
    }

    @AfterEach
    void tearDown() {
        ControllerTestSupport.clearCurrentRequest();
    }

    @Test
    @DisplayName("POST calcula o score e devolve 201 com o header Location")
    void registerShouldReturnCreatedWithLocation() {
        ControllerTestSupport.bindCurrentRequest("POST", BASE_URL);
        CalculatedScoreRequest request = mock(CalculatedScoreRequest.class);
        CalculatedScoreResponse response = mock(CalculatedScoreResponse.class);
        doReturn(12L).when(response).id();
        when(calculatedScoreService.create(request)).thenReturn(response);

        ResponseEntity<CalculatedScoreResponse> result = controller.register(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getHeaders().getLocation()).hasToString("http://localhost/api/v1/calculated-scores/12");
        assertThat(result.getBody()).isSameAs(response);
    }

    @Test
    @DisplayName("POST propaga a BusinessException quando não há configuração de pesos ativa")
    void registerShouldPropagateBusinessException() {
        CalculatedScoreRequest request = mock(CalculatedScoreRequest.class);
        when(calculatedScoreService.create(request))
                .thenThrow(new BusinessException("No active weight configuration found in the system."));

        assertThatThrownBy(() -> controller.register(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("POST propaga ResourceNotFoundException quando a empresa não existe")
    void registerShouldPropagateNotFound() {
        CalculatedScoreRequest request = mock(CalculatedScoreRequest.class);
        when(calculatedScoreService.create(request)).thenThrow(new ResourceNotFoundException("Firm not found with ID: 1"));

        assertThatThrownBy(() -> controller.register(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("GET /{id} devolve 200 com o score")
    void getShouldReturnOk() {
        CalculatedScoreResponse response = mock(CalculatedScoreResponse.class);
        when(calculatedScoreService.findById(12L)).thenReturn(response);

        ResponseEntity<CalculatedScoreResponse> result = controller.getCalculatedScore(12L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isSameAs(response);
    }

    @Test
    @DisplayName("GET lista sem parâmetros usa 10 itens por página ordenados por id")
    void listShouldUseDefaultPageable() throws Exception {
        when(calculatedScoreService.findAll(any())).thenReturn(ControllerTestSupport.emptyPage());

        mvc.perform(get(BASE_URL)).andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(calculatedScoreService).findAll(captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
        assertThat(captor.getValue().getSort().getOrderFor("id")).isNotNull();
    }

    @Test
    @DisplayName("PATCH /{id} recalcula e devolve 200")
    void updateShouldReturnOk() {
        UpdateCalculatedScoreRequest request = mock(UpdateCalculatedScoreRequest.class);
        CalculatedScoreResponse response = mock(CalculatedScoreResponse.class);
        when(calculatedScoreService.update(12L, request)).thenReturn(response);

        ResponseEntity<CalculatedScoreResponse> result = controller.update(12L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isSameAs(response);
    }

    @Test
    @DisplayName("DELETE /{id} devolve 204")
    void deleteShouldReturnNoContent() throws Exception {
        mvc.perform(delete(BASE_URL + "/12")).andExpect(status().isNoContent());

        verify(calculatedScoreService).delete(12L);
    }

    @Test
    @DisplayName("DELETE /{id} propaga a BusinessException ao tentar excluir o único score ativo")
    void deleteShouldPropagateBusinessException() {
        doThrow(new BusinessException("Cannot delete the only active calculated score."))
                .when(calculatedScoreService).delete(anyLong());

        assertThatThrownBy(() -> controller.delete(12L)).isInstanceOf(BusinessException.class);
    }
}
