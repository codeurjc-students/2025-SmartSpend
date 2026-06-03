package com.smartspend.system.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.smartspend.report.ReportController;
import com.smartspend.report.ReportService;
import com.smartspend.report.dtos.ReportResponseDTO;
import com.smartspend.report.dtos.StadisticsDto;

class ReportApiTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
    }

    @Test
    void shouldGetReportData() throws Exception {
        ReportResponseDTO response = new ReportResponseDTO(
            null,
            List.of(),
            List.of(),
            new StadisticsDto(1000f, 300f, 700f),
            null,
            null,
            null,
            null
        );

        when(reportService.getResponseData(3L, "user@test.com", 2026, 6)).thenReturn(response);

        Authentication authentication = new UsernamePasswordAuthenticationToken("user@test.com", null);

        mockMvc.perform(get("/api/v1/report/report-data")
                .principal(authentication)
                .param("bankAccountId", "3")
                .param("year", "2026")
                .param("month", "6"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stadistics.totalIncomes").value(1000.0))
            .andExpect(jsonPath("$.stadistics.totalExpenses").value(300.0));

        verify(reportService).getResponseData(3L, "user@test.com", 2026, 6);
    }

    @Test
    void shouldReturnBadRequestWhenReportFails() throws Exception {
        when(reportService.getResponseData(3L, "user@test.com", 2026, 6))
            .thenThrow(new RuntimeException("not allowed"));

        Authentication authentication = new UsernamePasswordAuthenticationToken("user@test.com", null);

        mockMvc.perform(get("/api/v1/report/report-data")
                .principal(authentication)
                .param("bankAccountId", "3")
                .param("year", "2026")
                .param("month", "6"))
            .andExpect(status().isBadRequest());
    }
}
