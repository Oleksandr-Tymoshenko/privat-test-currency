package com.example.privattest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.privattest.dto.DynamicDetailsDto;
import com.example.privattest.dto.ErrorResponseDto;
import com.example.privattest.dto.ExchangeRateDto;
import com.example.privattest.model.Currency;
import com.example.privattest.notification.impl.TelegramNotificationService;
import com.example.privattest.util.TimeProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "classpath:database.scripts/clear-exchange_rate-table.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@ActiveProfiles("test")
class ExchangeRateControllerTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2024, 12, 7, 12, 0);
    private static final String LATEST_API_URL = "/api/exchange-rate/latest";
    private static final String HOURLY_DIFFERENCE_API_URL = "/api/exchange-rate/hourly-difference";
    private static final String DAILY_DYNAMICS_API_URL = "/api/exchange-rate/daily-dynamics";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskScheduler taskScheduler;

    @MockBean
    private TimeProvider timeProvider;

    @MockBean
    private TelegramNotificationService notificationService;

    @BeforeEach
    void setUp() {
        Mockito.when(timeProvider.now()).thenReturn(NOW);
        Mockito.when(timeProvider.today()).thenReturn(NOW.toLocalDate());
    }

    @Test
    @DisplayName("getLatestRate - valid currency - returns latest rate")
    @Sql(scripts = "classpath:database.scripts/fill-exchange_rate-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @SneakyThrows
    void getLatestRate_whenValidCurrency_returnsLatestRate() {
        // Given
        ExchangeRateDto expected = new ExchangeRateDto(
                Currency.USD,
                BigDecimal.valueOf(41.320000).setScale(6, RoundingMode.HALF_UP),
                BigDecimal.valueOf(41.819650).setScale(6, RoundingMode.HALF_UP),
                NOW
        );

        // When
        MvcResult result = mockMvc.perform(get(LATEST_API_URL)
                        .param("currency", "USD"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        ExchangeRateDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                ExchangeRateDto.class);
        assertNotNull(actual);
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual, "timestamp"),
                String.format("Expected: %s, \n Actual: %s", expected, actual));
    }

    @Test
    @DisplayName("getLatestRate - invalid currency - returns BadRequest")
    @Sql(scripts = "classpath:database.scripts/fill-exchange_rate-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @SneakyThrows
    void getLatestRate_whenInvalidCurrency_returnsBadRequest() {
        mockMvc.perform(get(LATEST_API_URL)
                        .param("currency", "USDT"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(LATEST_API_URL)
                        .param("currency", "UAH"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getLatestRate - no data error - returns error response")
    @SneakyThrows
    void getLatestRate_whenNoDataError_returnsErrorResponse() {
        // Given
        ErrorResponseDto expectedError = new ErrorResponseDto(
                1,
                "Some reason"
        );

        // When
        MvcResult result = mockMvc.perform(get(LATEST_API_URL)
                        .param("currency", "USD"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        ErrorResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                ErrorResponseDto.class);
        assertNotNull(actual);
        assertTrue(EqualsBuilder.reflectionEquals(expectedError, actual, "reason"),
                String.format("Expected: %s, \n Actual: %s", expectedError, actual));
    }

    @Test
    @DisplayName("getHourlyDifference - valid currency - returns hourly difference")
    @Sql(scripts = "classpath:database.scripts/fill-exchange_rate-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @SneakyThrows
    void getHourlyDifference_whenValidCurrency_returnsHourlyDifference() {
        // Given
        DynamicDetailsDto expected = new DynamicDetailsDto(
                Currency.USD,
                BigDecimal.valueOf(0.133300).setScale(6, RoundingMode.HALF_UP),
                NOW.minusHours(1),
                BigDecimal.valueOf(0.059800).setScale(6, RoundingMode.HALF_UP),
                NOW
        );

        // When
        MvcResult result = mockMvc.perform(get(HOURLY_DIFFERENCE_API_URL)
                        .param("currency", "USD"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        DynamicDetailsDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                DynamicDetailsDto.class);
        assertNotNull(actual);
        assertTrue(EqualsBuilder.reflectionEquals(expected, actual,
                        "oldRateTimestamp", "newRateTimestamp"),
                String.format("Expected: %s, \n Actual: %s", expected, actual));
    }

    @Test
    @DisplayName("getHourlyDifference - invalid currency - returns BadRequest")
    @Sql(scripts = "classpath:database.scripts/fill-exchange_rate-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @SneakyThrows
    void getHourlyDifference_whenInvalidCurrency_returnsBadRequest() {
        mockMvc.perform(get(HOURLY_DIFFERENCE_API_URL)
                        .param("currency", "USDT"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(HOURLY_DIFFERENCE_API_URL)
                        .param("currency", "UAH"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getHourlyDifference - no data error - returns error response")
    @SneakyThrows
    void getHourlyDifference_whenNoDataError_returnsErrorResponse() {
        // Given
        ErrorResponseDto expectedError = new ErrorResponseDto(
                1,
                "Some reason"
        );

        // When
        MvcResult result = mockMvc.perform(get(HOURLY_DIFFERENCE_API_URL)
                        .param("currency", "USD"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        ErrorResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                ErrorResponseDto.class);
        assertNotNull(actual);
        assertTrue(EqualsBuilder.reflectionEquals(expectedError, actual, "reason"),
                String.format("Expected: %s, \n Actual: %s", expectedError, actual));
    }

    @Test
    @DisplayName("getDailyDynamics - valid currency - returns daily dynamics list")
    @Sql(scripts = "classpath:database.scripts/fill-exchange_rate-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @SneakyThrows
    void getDailyDynamics_whenValidCurrency_returnsDailyDynamicsList() {
        // Given
        List<DynamicDetailsDto> expected = List.of(
                new DynamicDetailsDto(
                        Currency.USD,
                        BigDecimal.valueOf(0.133300).setScale(6, RoundingMode.HALF_UP),
                        NOW,
                        BigDecimal.valueOf(0.059800).setScale(6, RoundingMode.HALF_UP),
                        NOW
                ),
                new DynamicDetailsDto(
                        Currency.USD,
                        BigDecimal.valueOf(0.036400).setScale(6, RoundingMode.HALF_UP),
                        NOW,
                        BigDecimal.valueOf(0.010500).setScale(6, RoundingMode.HALF_UP),
                        NOW
                )
        );

        // When
        MvcResult result = mockMvc.perform(get(DAILY_DYNAMICS_API_URL)
                        .param("currency", "USD"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<DynamicDetailsDto> actual = objectMapper
                .readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {}
                );
        assertEquals(expected.size(), actual.size());
        assertThat(actual)
                .usingElementComparatorIgnoringFields("newRateTimestamp", "oldRateTimestamp")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("getDailyDynamics - invalid currency - returns BadRequest")
    @Sql(scripts = "classpath:database.scripts/fill-exchange_rate-table.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @SneakyThrows
    void getDailyDynamics_whenInvalidCurrency_returnsBadRequest() {
        mockMvc.perform(get(DAILY_DYNAMICS_API_URL)
                        .param("currency", "USDT"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(DAILY_DYNAMICS_API_URL)
                        .param("currency", "UAH"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getDailyDynamics - no data error - returns error response")
    @SneakyThrows
    void getDailyDynamics_whenNoDataError_returnsErrorResponse() {
        // Given
        ErrorResponseDto expectedError = new ErrorResponseDto(
                1,
                "Some reason"
        );

        // When
        MvcResult result = mockMvc.perform(get(DAILY_DYNAMICS_API_URL)
                        .param("currency", "USD"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        ErrorResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                ErrorResponseDto.class);
        assertNotNull(actual);
        assertTrue(EqualsBuilder.reflectionEquals(expectedError, actual, "reason"),
                String.format("Expected: %s, \n Actual: %s", expectedError, actual));
    }
}
