package com.example.privattest.service;

import com.example.privattest.dto.BankRateApiResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for interacting with a bank's API to fetch exchange rate data.
 * Provides a method to asynchronously retrieve a list of exchange rates from the bank's API.
 *
 * @param <T> the type of the API response, extending {@link BankRateApiResponse}.
 */
public interface BankApiService<T extends BankRateApiResponse> {
    /**
     * Asynchronously fetches exchange rate data from the bank's API.
     *
     * @return a {@link CompletableFuture} containing a list of exchange rate data
     * as objects of type {@link T}.
     */
    CompletableFuture<List<T>> fetchRates();
}
