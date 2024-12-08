package com.example.privattest.service;

import com.example.privattest.dto.DynamicDetailsDto;
import com.example.privattest.dto.ExchangeRateDto;
import com.example.privattest.model.Currency;
import java.util.List;

/**
 * Service interface for managing exchange rates.
 * Provides methods to fetch the latest exchange rate, calculate dynamics,
 * and update the exchange rate data in the database.
 */
public interface ExchangeRateService {
    /**
     * Fetches the latest exchange rate for a given currency.
     *
     * @param currency the {@link Currency} for which the latest rate is requested.
     * @return an {@link ExchangeRateDto} containing the latest exchange rate information.
     */
    ExchangeRateDto getLatestRate(Currency currency);

    /**
     * Calculates the hourly dynamics of exchange rates for a given currency.
     * This includes percentage changes in buy and sell rates over the past hour.
     *
     * @param currency the {@link Currency} for which hourly dynamics are calculated.
     * @return a {@link DynamicDetailsDto} containing the dynamics data.
     */
    DynamicDetailsDto getHourlyDynamics(Currency currency);

    /**
     * Calculates the daily dynamics of exchange rates for a given currency.
     * It analyzes changes in rates at hourly intervals throughout the day.
     *
     * @param currency the {@link Currency} for which daily dynamics are calculated.
     * @return a list of {@link DynamicDetailsDto} objects,
     * each representing dynamics data for an hourly interval.
     */
    List<DynamicDetailsDto> getDailyDynamics(Currency currency);

    /**
     * Fetches the latest exchange rates from external sources and records them in the database.
     * The operation updates the cached exchange rates to ensure fresh data availability.
     */
    void updateExchangeRates();
}
