package com.example.privattest.service.impl;

import com.example.privattest.dto.MonoRateApiResponse;
import com.example.privattest.service.BankApiService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonoBankApiService implements BankApiService<MonoRateApiResponse> {
    private final WebClient webClient;

    @Value("${mono.currency.api}")
    private String apiUrl;

    @Override
    public CompletableFuture<List<MonoRateApiResponse>> fetchRates() {
        log.info("Starting to fetch currency rates from: {}", apiUrl);

        return webClient.get()
                .uri(apiUrl)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<MonoRateApiResponse>>() {})
                .doOnTerminate(() -> log.info("Finished fetching currency rates from: {}", apiUrl))
                .doOnError(error ->
                        log.error(
                                "Error occurred while fetching currency rates from: {}. Error: {}",
                                apiUrl, error.getMessage()
                        ))
                .toFuture();
    }
}
