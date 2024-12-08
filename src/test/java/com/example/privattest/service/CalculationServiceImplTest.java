package com.example.privattest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.privattest.dto.DynamicDetailsDto;
import com.example.privattest.model.Currency;
import com.example.privattest.model.ExchangeRate;
import com.example.privattest.service.impl.CalculationServiceImpl;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CalculationServiceImplTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2024, 12, 7, 12, 0);
    @InjectMocks
    private CalculationServiceImpl calculationService;

    @Test
    @DisplayName("calculateDynamicDetails - valid rate change - returns correct dynamic details")
    void calculateDynamicDetails_whenValidRateChange_returnsCorrectDynamicDetails() {
        // Given
        ExchangeRate oldRate = new ExchangeRate();
        oldRate.setRateBuy(BigDecimal.valueOf(36.50));
        oldRate.setRateSell(BigDecimal.valueOf(36.60));
        oldRate.setTimestamp(NOW.minusHours(1));

        ExchangeRate latestRate = new ExchangeRate();
        latestRate.setRateBuy(BigDecimal.valueOf(37.00));
        latestRate.setRateSell(BigDecimal.valueOf(37.10));
        latestRate.setTimestamp(NOW);
        Currency currency = Currency.USD;

        // When
        DynamicDetailsDto result = calculationService
                .calculateDynamicDetails(currency, oldRate, latestRate);

        // Then
        assertNotNull(result);
        assertEquals(currency, result.currency());
        assertEquals(BigDecimal.valueOf(1.369900)
                .setScale(6, RoundingMode.HALF_UP), result.percentageChangeBuy());
        assertEquals(BigDecimal.valueOf(1.366100)
                .setScale(6, RoundingMode.HALF_UP), result.percentageChangeSell());
    }

    @Test
    @DisplayName("calculateDynamicDetails - first rate is zero - returns 100% change")
    void calculateDynamicDetails_whenFirstRateIsZero_returns100PercentChange() {
        // Given
        ExchangeRate oldRate = new ExchangeRate();
        oldRate.setRateBuy(BigDecimal.valueOf(0));
        oldRate.setRateSell(BigDecimal.valueOf(0));
        oldRate.setTimestamp(NOW.minusHours(1));

        ExchangeRate latestRate = new ExchangeRate();
        latestRate.setRateBuy(BigDecimal.valueOf(37.00));
        latestRate.setRateSell(BigDecimal.valueOf(37.10));
        latestRate.setTimestamp(NOW);
        Currency currency = Currency.USD;

        // When
        DynamicDetailsDto result = calculationService
                .calculateDynamicDetails(currency, oldRate, latestRate);

        // Then
        assertNotNull(result);
        assertEquals(currency, result.currency());
        assertEquals(BigDecimal.valueOf(100), result.percentageChangeBuy());
        assertEquals(BigDecimal.valueOf(100), result.percentageChangeSell());
    }

    @Test
    @DisplayName("calculateDynamicDetails - both rates are zero - returns zero percentage change")
    void calculateDynamicDetails_whenBothRatesAreZero_returnsZeroPercentageChange() {
        // Given
        ExchangeRate oldRate = new ExchangeRate();
        oldRate.setRateBuy(BigDecimal.valueOf(0));
        oldRate.setRateSell(BigDecimal.valueOf(0));
        oldRate.setTimestamp(NOW.minusHours(1));

        ExchangeRate latestRate = new ExchangeRate();
        latestRate.setRateBuy(BigDecimal.valueOf(0));
        latestRate.setRateSell(BigDecimal.valueOf(0));
        latestRate.setTimestamp(NOW);
        Currency currency = Currency.USD;

        // When
        DynamicDetailsDto result = calculationService
                .calculateDynamicDetails(currency, oldRate, latestRate);

        // Then
        assertNotNull(result);
        assertEquals(currency, result.currency());
        assertEquals(BigDecimal.valueOf(0), result.percentageChangeBuy());
        assertEquals(BigDecimal.valueOf(0), result.percentageChangeSell());
    }

    @Test
    @DisplayName("calculateDailyDynamics - valid time difference - returns list with one change")
    void calculateDailyDynamics_whenTimeDifferenceWithinLimit_returnsListWithOneChange() {
        // Given
        Currency currency = Currency.USD;
        ExchangeRate rate1 = new ExchangeRate(
                null,
                currency,
                BigDecimal.valueOf(36.50),
                BigDecimal.valueOf(36.60),
                NOW.minusHours(1)
        );

        ExchangeRate rate2 = new ExchangeRate(
                null,
                currency,
                BigDecimal.valueOf(37.00),
                BigDecimal.valueOf(37.10),
                NOW
        );

        List<ExchangeRate> rates = Arrays.asList(rate1, rate2);

        ReflectionTestUtils.setField(calculationService, "maxDifference", 110L);

        // When
        List<DynamicDetailsDto> result = calculationService
                .calculateDailyDynamics(currency, rates);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("""
            calculateDailyDynamics - time difference exceeds maxDifference - returns empty list
            """)
    void calculateDailyDynamics_whenTimeDifferenceExceedsMaxDifference_returnsEmptyList() {
        // Given
        Currency currency = Currency.USD;
        ExchangeRate rate1 = new ExchangeRate(
                null,
                currency,
                BigDecimal.valueOf(36.50),
                BigDecimal.valueOf(36.60),
                NOW.minusHours(2)
        );

        ExchangeRate rate2 = new ExchangeRate(
                null,
                currency,
                BigDecimal.valueOf(37.00),
                BigDecimal.valueOf(37.10),
                NOW
        );

        List<ExchangeRate> rates = Arrays.asList(rate1, rate2);

        ReflectionTestUtils.setField(calculationService, "maxDifference", 110L);

        // When
        List<DynamicDetailsDto> result = calculationService
                .calculateDailyDynamics(currency, rates);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("calculateAverageRates - valid rates provided - returns average rates")
    void calculateAverageRates_whenValidRatesProvided_returnsAverageRates() {
        // Given
        ExchangeRate rate1 = new ExchangeRate();
        rate1.setCurrency(Currency.USD);
        rate1.setRateBuy(BigDecimal.valueOf(36.50));
        rate1.setRateSell(BigDecimal.valueOf(36.60));

        ExchangeRate rate2 = new ExchangeRate();
        rate2.setCurrency(Currency.USD);
        rate2.setRateBuy(BigDecimal.valueOf(37.00));
        rate2.setRateSell(BigDecimal.valueOf(37.10));

        List<ExchangeRate> rates = Arrays.asList(rate1, rate2);

        // When
        Map<Currency, ExchangeRate> result = calculationService.calculateAverageRates(rates);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey(Currency.USD));

        ExchangeRate averageRate = result.get(Currency.USD);
        assertEquals(
                BigDecimal.valueOf(36.75).setScale(2, RoundingMode.HALF_UP),
                averageRate.getRateBuy()
        );
        assertEquals(
                BigDecimal.valueOf(36.85).setScale(2, RoundingMode.HALF_UP),
                averageRate.getRateSell()
        );
    }
}
