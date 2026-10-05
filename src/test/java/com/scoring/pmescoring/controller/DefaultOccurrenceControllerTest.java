package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.dto.request.defaultoccurrence.DefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.request.defaultoccurrence.UpdateDefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.response.defaultoccurrence.DefaultOccurrenceResponse;
import com.scoring.pmescoring.service.DefaultOccurrenceService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DefaultOccurrenceControllerTest {

    private static final String BASE_URL = "/api/v1/default-occurrences";

    @Mock
    private DefaultOccurrenceService defaultOccurrenceService;

    @InjectMocks
    private DefaultOccurrenceController controller;

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
    @DisplayName("POST registra a ocorrência e devolve 201 com o header Location")
    void registerShouldReturnCreatedWithLocation() {
        ControllerTestSupport.bindCurrentRequest("POST", BASE_URL);
        DefaultOccurrenceRequest request = mock(DefaultOccurrenceRequest.class);
        DefaultOccurrenceResponse response = mock(DefaultOccurrenceResponse.class);
        doReturn(7L).when(response).id();
        when(defaultOccurrenceService.create(request)).thenReturn(response);

        ResponseEntity<DefaultOccurrenceResponse> result = controller.register(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getHeaders().getLocation()).hasToString("http://localhost/api/v1/default-occurrences/7");
        assertThat(result.getBody()).isSameAs(response);
    }

    @Test
    @DisplayName("POST propaga ResourceNotFoundException quando a empresa não existe")
    void registerShouldPropagateNotFound() {
        DefaultOccurrenceRequest request = mock(DefaultOccurrenceRequest.class);
        when(defaultOccurrenceService.create(request)).thenThrow(new ResourceNotFoundException("Firm not found with ID: 1"));

        assertThatThrownBy(() -> controller.register(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("GET /{id} devolve 200 com a ocorrência")
    void getShouldReturnOk() {
        DefaultOccurrenceResponse response = mock(DefaultOccurrenceResponse.class);
        when(defaultOccurrenceService.findById(7L)).thenReturn(response);

        ResponseEntity<DefaultOccurrenceResponse> result = controller.getDefaultOccurrence(7L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isSameAs(response);
    }

    @Test
    @DisplayName("GET lista sem parâmetros usa 10 itens por página ordenados por id")
    void listShouldUseDefaultPageable() throws Exception {
        when(defaultOccurrenceService.findAll(any())).thenReturn(ControllerTestSupport.emptyPage());

        mvc.perform(get(BASE_URL)).andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(defaultOccurrenceService).findAll(captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
        assertThat(captor.getValue().getSort().getOrderFor("id")).isNotNull();
    }

    @Test
    @DisplayName("PATCH /{id} atualiza os dados e devolve 200")
    void updateShouldReturnOk() {
        UpdateDefaultOccurrenceRequest request = mock(UpdateDefaultOccurrenceRequest.class);
        DefaultOccurrenceResponse response = mock(DefaultOccurrenceResponse.class);
        when(defaultOccurrenceService.update(7L, request)).thenReturn(response);

        ResponseEntity<DefaultOccurrenceResponse> result = controller.update(7L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isSameAs(response);
    }

    @Test
    @DisplayName("PATCH /{id}/status marca como resolvida e devolve 200")
    void updateStatusShouldReturnOk() {
        DefaultOccurrenceResponse response = mock(DefaultOccurrenceResponse.class);
        when(defaultOccurrenceService.updateStatus(7L)).thenReturn(response);

        ResponseEntity<DefaultOccurrenceResponse> result = controller.updateStatus(7L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isSameAs(response);
    }

    @Test
    @DisplayName("A rota PATCH /{id}/status chega ao updateStatus, e não ao update comum")
    void statusRouteShouldCallUpdateStatus() throws Exception {
        mvc.perform(patch(BASE_URL + "/7/status")).andExpect(status().isOk());

        verify(defaultOccurrenceService).updateStatus(7L);
        verify(defaultOccurrenceService, never()).update(any(), any());
    }

    @Test
    @DisplayName("DELETE /{id} devolve 204")
    void deleteShouldReturnNoContent() throws Exception {
        mvc.perform(delete(BASE_URL + "/7")).andExpect(status().isNoContent());

        verify(defaultOccurrenceService).delete(7L);
    }

    @Test
    @DisplayName("DELETE /{id} propaga ResourceNotFoundException para ocorrência inexistente")
    void deleteShouldPropagateNotFound() {
        doThrow(new ResourceNotFoundException("Default Occurrence not found with ID: 99"))
                .when(defaultOccurrenceService).delete(99L);

        assertThatThrownBy(() -> controller.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
