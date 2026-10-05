package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.common.exception.BusinessException;
import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.dto.request.weightconfiguration.UpdateWeightConfigurationRequest;
import com.scoring.pmescoring.dto.request.weightconfiguration.WeightConfigurationRequest;
import com.scoring.pmescoring.dto.response.weightconfiguration.WeightConfigurationResponse;
import com.scoring.pmescoring.service.WeightConfigurationService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WeightConfigurationControllerTest {

    private static final String BASE_URL = "/api/v1/weight-configurations";

    @Mock
    private WeightConfigurationService weightConfigurationService;

    @InjectMocks
    private WeightConfigurationController controller;

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
    @DisplayName("POST cria a configuração e devolve 201 com o header Location")
    void registerShouldReturnCreatedWithLocation() {
        ControllerTestSupport.bindCurrentRequest("POST", BASE_URL);
        WeightConfigurationRequest request = mock(WeightConfigurationRequest.class);
        WeightConfigurationResponse response = mock(WeightConfigurationResponse.class);
        doReturn(4L).when(response).id();
        when(weightConfigurationService.create(request)).thenReturn(response);

        ResponseEntity<WeightConfigurationResponse> result = controller.register(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getHeaders().getLocation()).hasToString("http://localhost/api/v1/weight-configurations/4");
        assertThat(result.getBody()).isSameAs(response);
    }

    @Test
    @DisplayName("POST propaga a BusinessException de limiares de risco inválidos")
    void registerShouldPropagateInvalidThresholds() {
        WeightConfigurationRequest request = mock(WeightConfigurationRequest.class);
        when(weightConfigurationService.create(request))
                .thenThrow(new BusinessException("Low risk threshold must be greater than medium risk threshold."));

        assertThatThrownBy(() -> controller.register(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("GET /{id} devolve 200 com a configuração")
    void getShouldReturnOk() {
        WeightConfigurationResponse response = mock(WeightConfigurationResponse.class);
        when(weightConfigurationService.findById(4L)).thenReturn(response);

        ResponseEntity<WeightConfigurationResponse> result = controller.getWeightConfiguration(4L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isSameAs(response);
    }

    @Test
    @DisplayName("GET /{id} propaga ResourceNotFoundException para configuração inexistente")
    void getShouldPropagateNotFound() {
        when(weightConfigurationService.findById(99L))
                .thenThrow(new ResourceNotFoundException("weight configuration not found with ID: 99"));

        assertThatThrownBy(() -> controller.getWeightConfiguration(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("GET lista o histórico com 10 itens por página ordenados por id")
    void listShouldUseDefaultPageable() throws Exception {
        when(weightConfigurationService.findAll(any())).thenReturn(ControllerTestSupport.emptyPage());

        mvc.perform(get(BASE_URL)).andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(weightConfigurationService).findAll(captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
        assertThat(captor.getValue().getSort().getOrderFor("id")).isNotNull();
    }

    @Test
    @DisplayName("PATCH /{id} cria a nova versão e devolve 200")
    void updateShouldReturnOk() {
        UpdateWeightConfigurationRequest request = mock(UpdateWeightConfigurationRequest.class);
        WeightConfigurationResponse response = mock(WeightConfigurationResponse.class);
        when(weightConfigurationService.update(4L, request)).thenReturn(response);

        ResponseEntity<WeightConfigurationResponse> result = controller.update(4L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isSameAs(response);
    }

    @Test
    @DisplayName("DELETE /{id} devolve 204")
    void deleteShouldReturnNoContent() throws Exception {
        mvc.perform(delete(BASE_URL + "/4")).andExpect(status().isNoContent());

        verify(weightConfigurationService).delete(4L);
    }

    @Test
    @DisplayName("DELETE /{id} propaga a BusinessException ao tentar excluir a única configuração ativa")
    void deleteShouldPropagateOnlyActiveRule() {
        doThrow(new BusinessException("Cannot delete the only active weight configuration."))
                .when(weightConfigurationService).delete(4L);

        assertThatThrownBy(() -> controller.delete(4L)).isInstanceOf(BusinessException.class);
    }
}
