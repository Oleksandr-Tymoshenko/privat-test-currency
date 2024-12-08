package com.example.privattest.service;

import com.example.privattest.dto.DynamicDetailsDto;
import com.example.privattest.model.Currency;
import com.example.privattest.model.ExchangeRate;
import java.util.List;
import java.util.Map;

/**
 * Service for performing various calculations related to exchange rates.
 */
public interface CalculationService {
    /**
     * Calculates the dynamic details between two exchange rates, including percentage changes
     * in buy and sell rates.
     *
     * @param currency   The currency for which the dynamics are calculated.
     * @param oldRate    The earlier exchange rate to compare.
     * @param latestRate The latest exchange rate to compare.
     * @return A {@link DynamicDetailsDto} containing the calculated changes in buy/sell rates
     * and their percentage differences.
     */
    DynamicDetailsDto calculateDynamicDetails(
            Currency currency, ExchangeRate oldRate, ExchangeRate latestRate
    );

    /**
     * Calculates the hourly dynamics for a given currency based on a list of exchange rates
     * recorded throughout the day.
     *
     * @param currency      The currency for which the daily dynamics are calculated.
     * @param exchangeRates A list of exchange rates for the specified currency, sorted
     *                      from newest to oldest.
     * @return A list of {@link DynamicDetailsDto}, each representing the change in rates
     * and their percentage differences for an hour interval.
     */
    List<DynamicDetailsDto> calculateDailyDynamics(
            Currency currency, List<ExchangeRate> exchangeRates
    );

    /**
     * Calculates the average buy and sell rates for each currency
     * from a given list of exchange rates.
     *
     * @param rates The list of {@link ExchangeRate} objects used to calculate the averages.
     * @return A map where the key is the {@link Currency} and the value is the {@link ExchangeRate}
     * object containing the calculated average buy and sell rates for that currency.
     */
    Map<Currency, ExchangeRate> calculateAverageRates(List<ExchangeRate> rates);
}
