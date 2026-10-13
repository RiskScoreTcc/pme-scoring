package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.common.util.PageableSanitizer;
import com.scoring.pmescoring.dto.response.firm.FirmRiskResponse;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.model.RiskBand;
import com.scoring.pmescoring.repository.FirmRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    private static final String HEADER = "CNPJ,Razao Social,Score,Faixa de Risco,Justificativa";

    @Mock private FirmRepository firmRepository;
    @Mock private PageableSanitizer pageableSanitizer;

    @InjectMocks
    private DashboardServiceImpl service;

    private FirmRiskResponse firmRisk(String cnpj, String name, Integer score, RiskBand band, String justification) {
        FirmRiskResponse response = mock(FirmRiskResponse.class);
        doReturn(cnpj).when(response).cnpj();
        doReturn(name).when(response).registeredCompanyName();
        doReturn(score).when(response).scoreValue();
        doReturn(band).when(response).riskBand();
        doReturn(justification).when(response).justification();
        return response;
    }

    private void givenPage(RiskBand filter, int pageNumber, int pageSize, long total, FirmRiskResponse... content) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        when(firmRepository.findActiveFirmsByRiskBand(EntityStatus.ACTIVE, EntityStatus.ACTIVE, filter, pageable))
                .thenReturn(new PageImpl<>(List.of(content), pageable, total));
    }

    private List<String> exportLines(RiskBand filter, int pageSize) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.exportFirmsWithScoresInCsv(filter, pageSize, out);
        return out.toString(StandardCharsets.UTF_8).lines().toList();
    }

    @Nested
    @DisplayName("getFirms()")
    class GetFirms {

        @Test
        @DisplayName("Sanitiza o Pageable e consulta apenas empresas e scores ativos com o filtro de risco")
        void shouldSanitizePageableAndDelegate() {
            Pageable requested = PageRequest.of(0, 5000);
            Pageable sanitized = PageRequest.of(0, 50);
            Page<FirmRiskResponse> page = new PageImpl<>(List.of());
            when(pageableSanitizer.sanitize(requested)).thenReturn(sanitized);
            when(firmRepository.findActiveFirmsByRiskBand(EntityStatus.ACTIVE, EntityStatus.ACTIVE, RiskBand.HIGH, sanitized))
                    .thenReturn(page);

            Page<FirmRiskResponse> result = service.getFirms(RiskBand.HIGH, requested);

            assertThat(result).isSameAs(page);
        }
    }

    @Nested
    @DisplayName("exportFirmsWithScoresInCsv()")
    class ExportCsv {

        @Test
        @DisplayName("Escreve só o cabeçalho quando não há empresas")
        void shouldWriteOnlyHeaderWhenEmpty() throws IOException {
            givenPage(null, 0, 10, 0);

            assertThat(exportLines(null, 10)).containsExactly(HEADER);
        }

        @Test
        @DisplayName("Formata cada empresa como uma linha CSV com textos entre aspas")
        void shouldFormatRow() throws IOException {
            givenPage(null, 0, 10, 1,
                    firmRisk("12345678000199", "Padaria Central", 650, RiskBand.MEDIUM, "Score médio"));

            assertThat(exportLines(null, 10)).containsExactly(
                    HEADER,
                    "\"12345678000199\",\"Padaria Central\",650,\"MEDIUM\",\"Score médio\""
            );
        }

        @Test
        @DisplayName("Duplica aspas internas e mantém vírgulas dentro do campo")
        void shouldEscapeQuotesAndKeepCommas() throws IOException {
            givenPage(null, 0, 10, 1,
                    firmRisk("1", "Mercado \"Bom Preço\" Ltda", 800, RiskBand.LOW, "Sem atrasos, faturamento alto"));

            List<String> lines = exportLines(null, 10);

            assertThat(lines.get(1)).isEqualTo(
                    "\"1\",\"Mercado \"\"Bom Preço\"\" Ltda\",800,\"LOW\",\"Sem atrasos, faturamento alto\"");
        }

        @Test
        @DisplayName("Campos nulos viram vazio, sem escrever \"null\"")
        void shouldWriteEmptyForNullValues() throws IOException {
            givenPage(null, 0, 10, 1, firmRisk("1", "Empresa sem score", null, null, null));

            List<String> lines = exportLines(null, 10);

            assertThat(lines.get(1)).isEqualTo("\"1\",\"Empresa sem score\",,\"\",\"\"");
            assertThat(lines.get(1)).doesNotContain("null");
        }

        @Test
        @DisplayName("Percorre todas as páginas até a última, respeitando o tamanho do lote")
        void shouldIterateThroughAllPages() throws IOException {
            givenPage(null, 0, 2, 5, firmRisk("1", "A", 100, RiskBand.HIGH, "j"), firmRisk("2", "B", 200, RiskBand.HIGH, "j"));
            givenPage(null, 1, 2, 5, firmRisk("3", "C", 300, RiskBand.HIGH, "j"), firmRisk("4", "D", 400, RiskBand.MEDIUM, "j"));
            givenPage(null, 2, 2, 5, firmRisk("5", "E", 500, RiskBand.MEDIUM, "j"));

            List<String> lines = exportLines(null, 2);

            assertThat(lines).hasSize(6);
            assertThat(lines.subList(1, 6))
                    .extracting(line -> line.substring(0, 3))
                    .containsExactly("\"1\"", "\"2\"", "\"3\"", "\"4\"", "\"5\"");
            verify(firmRepository, times(3)).findActiveFirmsByRiskBand(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Repassa o filtro de faixa de risco para a consulta")
        void shouldPassRiskFilter() throws IOException {
            givenPage(RiskBand.LOW, 0, 10, 0);

            exportLines(RiskBand.LOW, 10);

            verify(firmRepository).findActiveFirmsByRiskBand(
                    EntityStatus.ACTIVE, EntityStatus.ACTIVE, RiskBand.LOW, PageRequest.of(0, 10));
        }

        @Test
        @DisplayName("Grava em UTF-8, preservando acentos")
        void shouldWriteUtf8() throws IOException {
            givenPage(null, 0, 10, 1, firmRisk("1", "Construções São João", 700, RiskBand.LOW, "Ação"));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            service.exportFirmsWithScoresInCsv(null, 10, out);

            assertThat(out.toString(StandardCharsets.UTF_8)).contains("Construções São João", "Ação");
        }

        @Test
        @DisplayName("Propaga IOException quando a escrita no stream falha")
        void shouldPropagateIOException() {
            givenPage(null, 0, 10, 0);
            OutputStream failingStream = new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    throw new IOException("Conexão encerrada pelo cliente");
                }
            };

            assertThatThrownBy(() -> service.exportFirmsWithScoresInCsv(null, 10, failingStream))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Conexão encerrada");
        }
    }
}
