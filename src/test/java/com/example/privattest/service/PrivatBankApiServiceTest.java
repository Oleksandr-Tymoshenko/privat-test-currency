package com.example.privattest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.example.privattest.dto.PrivatRateApiResponse;
import com.example.privattest.service.impl.PrivatBankApiService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PrivatBankApiServiceTest {
    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private PrivatBankApiService privatBankApiService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(privatBankApiService, "apiUrl", "https://test.com");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("fetchRates - successful response - returns expected list of rates")
    @SneakyThrows
    void fetchRates_whenResponseIsSuccessful_returnsExpectedRates() {
        // Given
        List<PrivatRateApiResponse> expectedResponse = List.of(new PrivatRateApiResponse());
        when(responseSpec.bodyToMono(ArgumentMatchers
                .<ParameterizedTypeReference<List<PrivatRateApiResponse>>>any()))
                .thenReturn(Mono.just(expectedResponse));

        // When
        CompletableFuture<List<PrivatRateApiResponse>> future = privatBankApiService.fetchRates();

        // Then
        assertNotNull(future);
        List<PrivatRateApiResponse> actualResponse = future.get();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("fetchRates - error response - throws ExecutionException")
    @SneakyThrows
    void fetchRates_whenResponseHasError_throwsExecutionException() {
        // Given
        when(responseSpec.bodyToMono(ArgumentMatchers
                .<ParameterizedTypeReference<List<PrivatRateApiResponse>>>any()))
                .thenReturn(Mono.error(new WebClientResponseException(
                        "Error fetching data", 500, "Internal Server Error", null, null, null)));

        // When
        CompletableFuture<List<PrivatRateApiResponse>> future = privatBankApiService.fetchRates();

        // Then
        assertThrows(ExecutionException.class, future::get);
    }
}
