package com.example.privattest.service.impl;

import com.example.privattest.dto.BankRateApiResponse;
import com.example.privattest.dto.DynamicDetailsDto;
import com.example.privattest.dto.ExchangeRateDto;
import com.example.privattest.exception.CurrencyDataNotFoundException;
import com.example.privattest.mapper.ExchangeRateMapper;
import com.example.privattest.model.Currency;
import com.example.privattest.model.ExchangeRate;
import com.example.privattest.notification.NotificationService;
import com.example.privattest.repository.ExchangeRateRepository;
import com.example.privattest.service.BankApiService;
import com.example.privattest.service.CalculationService;
import com.example.privattest.service.ExchangeRateService;
import com.example.privattest.util.TimeProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateServiceImpl implements ExchangeRateService {
    private final List<BankApiService<?>> bankServices;
    private final CalculationService calculationService;
    private final NotificationService notificationService;
    private final ExchangeRateMapper exchangeRateMapper;
    private final ExchangeRateRepository exchangeRateRepository;
    private final TimeProvider timeProvider;

    @Value("${max.minutes.difference-between-rates}")
    private Long maxDifference;

    @Override
    @Cacheable(value = "exchangeRates", key = "#currency")
    public ExchangeRateDto getLatestRate(Currency currency) {
        log.debug("Fetching the latest exchange rate for currency: {}...", currency);
        ExchangeRate latestRate = getLatestExchangeRate(currency);
        log.info("Fetched the latest exchange rate for currency: {}. Rate: {}",
                currency, latestRate);
        return exchangeRateMapper.toDto(latestRate);
    }

    @Override
    @Cacheable(value = "dynamicDetails", key = "#currency")
    public DynamicDetailsDto getHourlyDynamics(Currency currency) {
        log.debug("Calculating hourly dynamics for currency: {}", currency);
        ExchangeRate latestRate = getLatestExchangeRate(currency);

        LocalDateTime oneHourAgo = latestRate.getTimestamp().minusMinutes(maxDifference);
        ExchangeRate oldRate = exchangeRateRepository
                .findTopByCurrencyAndTimestampBetweenOrderByTimestampDesc(
                        currency,
                        oneHourAgo,
                        latestRate.getTimestamp().minusMinutes(1)
                )
                .orElseThrow(() -> new CurrencyDataNotFoundException(
                        "For the last hour for currency " + currency
                ));
        DynamicDetailsDto dynamicDetailsDto = calculationService
                .calculateDynamicDetails(currency, oldRate, latestRate);
        log.info("Calculated hourly dynamics for currency: {}. Dynamics: {}",
                currency, dynamicDetailsDto);
        return dynamicDetailsDto;
    }

    @Override
    @Cacheable(value = "dynamicDetailsList", key = "#currency")
    public List<DynamicDetailsDto> getDailyDynamics(Currency currency) {
        log.debug("Fetching daily rate changes for currency: {}", currency);
        LocalDateTime startOfDay = timeProvider.today().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        List<ExchangeRate> exchangeRates = exchangeRateRepository
                .findAllByCurrencyAndTimestampBetweenOrderByTimestampDesc(
                        currency, startOfDay, endOfDay
                );

        if (exchangeRates.isEmpty()) {
            throw new CurrencyDataNotFoundException(String.format(
                    "No data for currency %s for today",
                    currency
            ));
        }
        List<DynamicDetailsDto> dailyDynamics = calculationService
                .calculateDailyDynamics(currency, exchangeRates);
        log.info("Calculated daily dynamics for currency: {}. Dynamic: {}",
                currency, dailyDynamics);
        return dailyDynamics;
    }

    @Override
    @CacheEvict(value = {"exchangeRates", "dynamicDetails", "dynamicDetailsList"},
            allEntries = true)
    @Scheduled(cron = "${scheduler.cron.every-hour}")
    public void updateExchangeRates() {
        log.debug("Updating exchange rates...");
        try {
            List<CompletableFuture<List<ExchangeRate>>> futures = bankServices.stream()
                    .map(service -> service.fetchRates().thenApply(this::mapToExchangeRates))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            List<ExchangeRate> exchangeRates = futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .toList();
            // Calculate the average exchange rates for each currency
            Map<Currency, ExchangeRate> averageRates = calculationService
                    .calculateAverageRates(exchangeRates);
            exchangeRateRepository.saveAll(averageRates.values());

            log.info("Exchange rates successfully fetched and saved. Saved data: {}",
                    averageRates.values());
            notificationService.notify(averageRates.values().stream().toList());
        } catch (Exception e) {
            log.error("Error fetching or saving exchange rates: {}", e.getMessage());
        }
    }

    private ExchangeRate getLatestExchangeRate(Currency currency) {
        return exchangeRateRepository
                .findTopByCurrencyOrderByTimestampDesc(currency)
                .orElseThrow(() -> new CurrencyDataNotFoundException(currency.name()));
    }

    /**
     * Maps a list of BankRateApiResponse objects to ExchangeRate objects.
     *
     * @param rates The list of BankRateApiResponse objects that need to be mapped.
     * @return A list of ExchangeRate objects after mapping and filtering out null currencies.
     */
    private List<ExchangeRate> mapToExchangeRates(List<? extends BankRateApiResponse> rates) {
        log.debug("Mapping bank API responses to exchange rate entities...");
        return rates.stream()
                .map(exchangeRateMapper::toExchangeRate)
                .filter(rate -> rate.getCurrency() != null)
                .toList();
    }
}
