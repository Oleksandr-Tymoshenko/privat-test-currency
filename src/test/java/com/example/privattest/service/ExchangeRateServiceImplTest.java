package com.example.privattest.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.privattest.dto.DynamicDetailsDto;
import com.example.privattest.dto.ExchangeRateDto;
import com.example.privattest.dto.PrivatRateApiResponse;
import com.example.privattest.exception.CurrencyDataNotFoundException;
import com.example.privattest.mapper.ExchangeRateMapper;
import com.example.privattest.model.Currency;
import com.example.privattest.model.ExchangeRate;
import com.example.privattest.notification.impl.TelegramNotificationService;
import com.example.privattest.repository.ExchangeRateRepository;
import com.example.privattest.service.impl.ExchangeRateServiceImpl;
import com.example.privattest.service.impl.PrivatBankApiService;
import com.example.privattest.util.TimeProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceImplTest {
    private static final long MAX_DIFFERENCE = 110L;
    private static final LocalDateTime NOW = LocalDateTime.of(2024, 12, 7, 12, 0);
    private static final LocalDateTime START_OF_DAY = LocalDate.of(2024, 12, 7).atStartOfDay();
    private static final LocalDateTime END_OF_DAY = START_OF_DAY.plusDays(1).minusNanos(1);

    @InjectMocks
    private ExchangeRateServiceImpl exchangeRateService;

    @Mock
    private PrivatBankApiService privatBankApiService;

    @Mock
    private CalculationService calculationService;

    @Mock
    private ExchangeRateMapper exchangeRateMapper;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private TimeProvider timeProvider;

    @Mock
    private TelegramNotificationService notificationService;

    @Test
    @DisplayName("getLatestRate - valid currency provided - returns latest exchange rate")
    void getLatestRate_whenValidCurrencyProvided_returnsLatestExchangeRate() {
        // Given
        Currency currency = Currency.USD;
        ExchangeRate latestRate = new ExchangeRate(
                null,
                currency,
                BigDecimal.valueOf(37.00),
                BigDecimal.valueOf(37.10),
                NOW
        );

        ExchangeRateDto expectedDto = new ExchangeRateDto(
                currency,
                latestRate.getRateBuy(),
                latestRate.getRateSell(),
                NOW
        );

        when(exchangeRateRepository.findTopByCurrencyOrderByTimestampDesc(currency))
                .thenReturn(Optional.of(latestRate));
        when(exchangeRateMapper.toDto(latestRate)).thenReturn(expectedDto);

        // When
        ExchangeRateDto result = exchangeRateService.getLatestRate(currency);

        // Then
        assertNotNull(result);
        assertEquals(currency, result.currency());
        assertEquals(latestRate.getRateBuy(), result.rateBuy());
        assertEquals(latestRate.getRateSell(), result.rateSell());
        verify(exchangeRateRepository, times(1))
                .findTopByCurrencyOrderByTimestampDesc(currency);
        verify(exchangeRateMapper, times(1)).toDto(latestRate);
    }

    @Test
    @DisplayName("""
            getLatestRate - repository returns empty Optional - throws CurrencyDataNotFoundException
            """)
    void getLatestRate_whenRepositoryReturnsEmptyOptional_throwsCurrencyDataNotFoundException() {
        // Given
        Currency currency = Currency.USD;

        when(exchangeRateRepository.findTopByCurrencyOrderByTimestampDesc(currency))
                .thenReturn(Optional.empty());

        // When / Then
        assertThrows(CurrencyDataNotFoundException.class, () -> exchangeRateService
                .getLatestRate(currency));
    }

    @Test
    @DisplayName("Test getHourlyDynamics - valid dynamic details")
    void getHourlyDynamics_validRate_returnDynamicDetails() {
        // Given
        ReflectionTestUtils.setField(exchangeRateService, "maxDifference", MAX_DIFFERENCE);
        Currency currency = Currency.USD;
        ExchangeRate latestRate = new ExchangeRate(
                null,
                currency,
                BigDecimal.valueOf(37.00),
                BigDecimal.valueOf(37.10),
                NOW
        );

        LocalDateTime hourAgo = NOW.minusHours(1);
        ExchangeRate oldRate = new ExchangeRate(
                null,
                currency,
                BigDecimal.valueOf(36.50),
                BigDecimal.valueOf(36.60),
                NOW
        );

        DynamicDetailsDto expectedDynamic = new DynamicDetailsDto(
                currency,
                BigDecimal.valueOf(1.37),
                hourAgo,
                BigDecimal.valueOf(1.36),
                NOW
        );

        when(exchangeRateRepository.findTopByCurrencyOrderByTimestampDesc(currency))
                .thenReturn(Optional.of(latestRate));
        when(exchangeRateRepository.findTopByCurrencyAndTimestampBetweenOrderByTimestampDesc(
                currency,
                latestRate.getTimestamp().minusMinutes(MAX_DIFFERENCE),
                latestRate.getTimestamp().minusMinutes(1)
        )).thenReturn(Optional.of(oldRate));

        when(calculationService.calculateDynamicDetails(currency, oldRate, latestRate))
                .thenReturn(expectedDynamic);

        // When
        DynamicDetailsDto result = exchangeRateService.getHourlyDynamics(currency);

        // Then
        assertNotNull(result);
        assertEquals(currency, result.currency());
        assertEquals(expectedDynamic.percentageChangeBuy(), result.percentageChangeBuy());
        assertEquals(expectedDynamic.percentageChangeSell(), result.percentageChangeSell());
        verify(exchangeRateRepository, times(1))
                .findTopByCurrencyAndTimestampBetweenOrderByTimestampDesc(
                        currency, latestRate.getTimestamp().minusMinutes(MAX_DIFFERENCE),
                        latestRate.getTimestamp().minusMinutes(1)
                );
        verify(calculationService, times(1))
                .calculateDynamicDetails(currency, oldRate, latestRate);
    }

    @Test
    @DisplayName("""
            getHourlyDynamics - big distance between rates - throws CurrencyDataNotFoundException
            """)
    void getHourlyDynamics_whenBigDistanceBetweenRates_throwsCurrencyDataNotFoundException() {
        // Given
        ReflectionTestUtils.setField(exchangeRateService, "maxDifference", MAX_DIFFERENCE);
        Currency currency = Currency.USD;
        ExchangeRate latestRate = new ExchangeRate(
                null,
                currency,
                BigDecimal.valueOf(37.00),
                BigDecimal.valueOf(37.10),
                NOW
        );

        when(exchangeRateRepository.findTopByCurrencyOrderByTimestampDesc(currency))
                .thenReturn(Optional.of(latestRate));

        when(exchangeRateRepository.findTopByCurrencyAndTimestampBetweenOrderByTimestampDesc(
                eq(currency), any(), any())
        ).thenReturn(Optional.empty());

        // When / Then
        assertThrows(
                CurrencyDataNotFoundException.class,
                () -> exchangeRateService.getHourlyDynamics(currency)
        );
    }

    @Test
    @DisplayName("getDailyDynamics - valid rates provided - returns valid daily dynamics")
    void getDailyDynamics_whenValidRatesProvided_returnsValidDailyDynamics() {
        // Given
        Currency currency = Currency.USD;

        ExchangeRate rate1 = new ExchangeRate(
                null,
                currency,
                BigDecimal.valueOf(36.50),
                BigDecimal.valueOf(36.60),
                START_OF_DAY.plusHours(1)
        );
        ExchangeRate rate2 = new ExchangeRate(
                null,
                currency,
                BigDecimal.valueOf(37.00),
                BigDecimal.valueOf(37.10),
                START_OF_DAY.plusHours(2)
        );

        List<ExchangeRate> exchangeRates = Arrays.asList(rate1, rate2);
        List<DynamicDetailsDto> expectedDailyDynamics = List.of(
                new DynamicDetailsDto(
                        currency,
                        BigDecimal.valueOf(1.37),
                        START_OF_DAY.plusHours(1),
                        BigDecimal.valueOf(1.36),
                        START_OF_DAY.plusHours(2)
                )
        );

        when(exchangeRateRepository.findAllByCurrencyAndTimestampBetweenOrderByTimestampDesc(
                currency, START_OF_DAY, END_OF_DAY
        )).thenReturn(exchangeRates);
        when(calculationService.calculateDailyDynamics(currency, exchangeRates))
                .thenReturn(expectedDailyDynamics);
        Mockito.when(timeProvider.today()).thenReturn(NOW.toLocalDate());

        // When
        List<DynamicDetailsDto> result = exchangeRateService.getDailyDynamics(currency);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(
                expectedDailyDynamics.get(0).percentageChangeBuy(),
                result.get(0).percentageChangeBuy()
        );
        assertEquals(
                expectedDailyDynamics.get(0).percentageChangeSell(),
                result.get(0).percentageChangeSell()
        );
        verify(exchangeRateRepository, times(1))
                .findAllByCurrencyAndTimestampBetweenOrderByTimestampDesc(
                        currency, START_OF_DAY, END_OF_DAY
                );
        verify(calculationService, times(1))
                .calculateDailyDynamics(currency, exchangeRates);
    }

    @Test
    @DisplayName("""
            getDailyDynamics - no rates found for the day - throws CurrencyDataNotFoundException
            """)
    void getDailyDynamics_whenNoRatesFoundForTheDay_throwsCurrencyDataNotFoundException() {
        // Given
        Currency currency = Currency.USD;

        when(exchangeRateRepository.findAllByCurrencyAndTimestampBetweenOrderByTimestampDesc(
                currency, START_OF_DAY, END_OF_DAY
        )).thenReturn(Collections.emptyList());
        Mockito.when(timeProvider.today()).thenReturn(NOW.toLocalDate());

        // When / Then
        assertThrows(
                CurrencyDataNotFoundException.class,
                () -> exchangeRateService.getDailyDynamics(currency)
        );
    }

    @Test
    @DisplayName("updateExchangeRates - successfully fetches rates from API and saves them")
    void updateExchangeRates_whenSuccessfullyFetchedRates_savesRates() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<BankApiService<?>> bankApiServices = List.of(
                privatBankApiService
        );
        exchangeRateService = new ExchangeRateServiceImpl(
                bankApiServices,
                calculationService,
                notificationService,
                exchangeRateMapper,
                exchangeRateRepository,
                timeProvider
        );

        List<PrivatRateApiResponse> privatExchangeRates = List.of(
                new PrivatRateApiResponse(
                        Currency.USD,
                        Currency.UAH,
                        "35.00",
                        "37.00"
                ),
                new PrivatRateApiResponse(
                        Currency.EUR,
                        Currency.UAH,
                        "36.00",
                        "38.00"
                )
        );
        List<ExchangeRate> privatExchangeRatesMapped = List.of(
                new ExchangeRate(null,
                        Currency.USD,
                        BigDecimal.valueOf(35.00),
                        BigDecimal.valueOf(37.00),
                        now
                ),
                new ExchangeRate(null,
                        Currency.EUR,
                        BigDecimal.valueOf(36.00),
                        BigDecimal.valueOf(38.00),
                        now
                ));

        when(privatBankApiService.fetchRates())
                .thenReturn(CompletableFuture.completedFuture(privatExchangeRates));
        when(exchangeRateMapper.toExchangeRate(privatExchangeRates.get(0)))
                .thenReturn(privatExchangeRatesMapped.get(0));
        when(exchangeRateMapper.toExchangeRate(privatExchangeRates.get(1)))
                .thenReturn(privatExchangeRatesMapped.get(1));

        List<ExchangeRate> exchangeRatesToSave = List.of(
                new ExchangeRate(null,
                        Currency.USD,
                        BigDecimal.valueOf(35.00),
                        BigDecimal.valueOf(37.00),
                        now
                ),
                new ExchangeRate(null,
                        Currency.EUR,
                        BigDecimal.valueOf(36.00),
                        BigDecimal.valueOf(38.00),
                        now
                ));

        Map<Currency, ExchangeRate> averageRates = new HashMap<>();
        averageRates.put(Currency.USD, exchangeRatesToSave.get(0));
        averageRates.put(Currency.EUR, exchangeRatesToSave.get(1));

        when(calculationService.calculateAverageRates(anyList())).thenReturn(averageRates);
        when(exchangeRateRepository.saveAll(anyCollection())).thenReturn(exchangeRatesToSave);

        // When
        exchangeRateService.updateExchangeRates();

        // Then
        verify(exchangeRateRepository, times(1)).saveAll(anyCollection());
        verify(exchangeRateMapper, times(2)).toExchangeRate(any());
        verify(calculationService, times(1))
                .calculateAverageRates(anyList());
    }

    @Test
    @DisplayName("updateExchangeRates - handles error when fetching rates from API")
    void updateExchangeRates_whenErrorOccursWhileFetchingRates_doesNotSaveRates() {
        // Given
        CompletableFuture<List<PrivatRateApiResponse>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("API Error"));

        // When / Then
        assertDoesNotThrow(() -> exchangeRateService.updateExchangeRates());
        verify(exchangeRateRepository, times(0)).saveAll(anyList());
    }
}
