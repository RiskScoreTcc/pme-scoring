package com.scoring.pmescoring.controller;

import com.scoring.pmescoring.model.RiskBand;
import com.scoring.pmescoring.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private static final String BASE_URL = "/api/v1/companies/dashboard";

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController controller;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = ControllerTestSupport.standalone(controller);
    }

    @Nested
    @DisplayName("GET /firms")
    class Firms {

        @Test
        @DisplayName("Sem filtro, passa risco nulo e 10 itens por página")
        void shouldUseDefaults() throws Exception {
            when(dashboardService.getFirms(any(), any())).thenReturn(ControllerTestSupport.emptyPage());

            mvc.perform(get(BASE_URL + "/firms")).andExpect(status().isOk());

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(dashboardService).getFirms(isNull(), captor.capture());
            assertThat(captor.getValue().getPageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("Converte o parâmetro risk=HIGH para o enum RiskBand")
        void shouldBindRiskFilter() throws Exception {
            when(dashboardService.getFirms(any(), any())).thenReturn(ControllerTestSupport.emptyPage());

            mvc.perform(get(BASE_URL + "/firms").param("risk", "HIGH")).andExpect(status().isOk());

            verify(dashboardService).getFirms(eq(RiskBand.HIGH), any(Pageable.class));
        }

        @Test
        @DisplayName("Faixa de risco inexistente devolve 400 sem chamar o service")
        void shouldRejectUnknownRiskBand() throws Exception {
            mvc.perform(get(BASE_URL + "/firms").param("risk", "ALTISSIMO")).andExpect(status().isBadRequest());

            verifyNoInteractions(dashboardService);
        }
    }

    @Nested
    @DisplayName("GET /export/csv")
    class ExportCsv {

        @Test
        @DisplayName("Devolve 200 com Content-Type text/csv e arquivo para download com nome datado")
        void shouldReturnCsvHeaders() {
            ResponseEntity<StreamingResponseBody> result = controller.exportLargeCsv(RiskBand.LOW, 100);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("text/csv"));
            assertThat(result.getHeaders().getContentDisposition().isAttachment()).isTrue();
            assertThat(result.getHeaders().getContentDisposition().getFilename())
                    .matches("firms-report-\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}\\.csv");
        }

        @Test
        @DisplayName("Só gera o CSV quando o stream é escrito, repassando filtro e tamanho do lote")
        void shouldDelegateWhenStreamIsWritten() throws IOException {
            ResponseEntity<StreamingResponseBody> result = controller.exportLargeCsv(RiskBand.LOW, 100);
            verifyNoInteractions(dashboardService); // nada é consultado antes de o cliente começar a baixar

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            result.getBody().writeTo(out);

            verify(dashboardService).exportFirmsWithScoresInCsv(RiskBand.LOW, 100, out);
        }

        @Test
        @DisplayName("Propaga a IOException ocorrida durante a escrita do stream")
        void shouldPropagateIOException() throws IOException {
            doThrow(new IOException("Conexão encerrada pelo cliente"))
                    .when(dashboardService).exportFirmsWithScoresInCsv(any(), anyInt(), any());
            ResponseEntity<StreamingResponseBody> result = controller.exportLargeCsv(null, 100);

            assertThatThrownBy(() -> result.getBody().writeTo(new ByteArrayOutputStream()))
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("Pela rota HTTP, sem parâmetros, usa lote padrão de 5000 e nenhum filtro")
        void routeShouldUseDefaultPageSize() throws Exception {
            MvcResult asyncResult = mvc.perform(get(BASE_URL + "/export/csv"))
                    .andExpect(request().asyncStarted())
                    .andReturn();

            mvc.perform(asyncDispatch(asyncResult)).andExpect(status().isOk());

            verify(dashboardService).exportFirmsWithScoresInCsv(isNull(), eq(5000), any(OutputStream.class));
        }
    }
}
