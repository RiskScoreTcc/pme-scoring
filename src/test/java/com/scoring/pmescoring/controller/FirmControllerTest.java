package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.common.exception.BusinessException;
import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.dto.request.firm.FirmRequest;
import com.scoring.pmescoring.dto.request.firm.UpdateFirmRequest;
import com.scoring.pmescoring.dto.response.firm.FirmResponse;
import com.scoring.pmescoring.service.FirmService;
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
import org.springframework.data.domain.Sort;
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
class FirmControllerTest {

    private static final String BASE_URL = "/api/v1/companies";

    @Mock
    private FirmService firmService;

    @InjectMocks
    private FirmController controller;

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
    @DisplayName("POST cadastra a empresa e devolve 201 com o header Location apontando para o novo recurso")
    void registerShouldReturnCreatedWithLocation() {
        ControllerTestSupport.bindCurrentRequest("POST", BASE_URL);
        FirmRequest request = mock(FirmRequest.class);
        FirmResponse response = mock(FirmResponse.class);
        doReturn(5L).when(response).id();
        when(firmService.create(request)).thenReturn(response);

        ResponseEntity<FirmResponse> result = controller.register(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getHeaders().getLocation()).hasToString("http://localhost/api/v1/companies/5");
        assertThat(result.getBody()).isSameAs(response);
    }

    @Test
    @DisplayName("POST propaga a BusinessException de CNPJ duplicado")
    void registerShouldPropagateBusinessException() {
        FirmRequest request = mock(FirmRequest.class);
        when(firmService.create(request)).thenThrow(new BusinessException("Firm already exists with this cnpj."));

        assertThatThrownBy(() -> controller.register(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("GET /{id} devolve 200 com a empresa")
    void getFirmShouldReturnOk() {
        FirmResponse response = mock(FirmResponse.class);
        when(firmService.findById(5L)).thenReturn(response);

        ResponseEntity<FirmResponse> result = controller.getFirm(5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isSameAs(response);
    }

    @Test
    @DisplayName("GET /{id} propaga ResourceNotFoundException para empresa inexistente")
    void getFirmShouldPropagateNotFound() {
        when(firmService.findById(99L)).thenThrow(new ResourceNotFoundException("Firm not found with ID: 99"));

        assertThatThrownBy(() -> controller.getFirm(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("GET /{id} com id não numérico devolve 400 sem chamar o service")
    void getFirmWithInvalidIdShouldReturnBadRequest() throws Exception {
        mvc.perform(get(BASE_URL + "/abc")).andExpect(status().isBadRequest());

        verifyNoInteractions(firmService);
    }

    @Test
    @DisplayName("GET lista sem parâmetros usa a paginação padrão: 10 itens ordenados por id")
    void listShouldUseDefaultPageable() throws Exception {
        when(firmService.findAll(any())).thenReturn(ControllerTestSupport.emptyPage());

        mvc.perform(get(BASE_URL)).andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(firmService).findAll(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
        assertThat(captor.getValue().getSort().getOrderFor("id")).isNotNull();
    }

    @Test
    @DisplayName("GET lista respeita page, size e sort enviados na URL")
    void listShouldUseRequestedPageable() throws Exception {
        when(firmService.findAll(any())).thenReturn(ControllerTestSupport.emptyPage());

        mvc.perform(get(BASE_URL).param("page", "2").param("size", "5").param("sort", "registeredCompanyName,desc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(firmService).findAll(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
        assertThat(captor.getValue().getSort().getOrderFor("registeredCompanyName").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("PATCH /{id} atualiza e devolve 200 com a empresa atualizada")
    void updateShouldReturnOk() {
        UpdateFirmRequest request = mock(UpdateFirmRequest.class);
        FirmResponse response = mock(FirmResponse.class);
        when(firmService.update(5L, request)).thenReturn(response);

        ResponseEntity<FirmResponse> result = controller.update(5L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isSameAs(response);
    }

    @Test
    @DisplayName("DELETE /{id} devolve 204 e aciona a exclusão em cascata no service")
    void deleteShouldReturnNoContent() throws Exception {
        mvc.perform(delete(BASE_URL + "/5")).andExpect(status().isNoContent());

        verify(firmService).delete(5L);
    }

    @Test
    @DisplayName("DELETE /{id} propaga ResourceNotFoundException para empresa inexistente")
    void deleteShouldPropagateNotFound() {
        doThrow(new ResourceNotFoundException("Firm not found with ID: 99")).when(firmService).delete(anyLong());

        assertThatThrownBy(() -> controller.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
