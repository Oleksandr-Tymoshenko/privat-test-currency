package com.example.privattest.controller;

import com.example.privattest.dto.DynamicDetailsDto;
import com.example.privattest.dto.ErrorResponseDto;
import com.example.privattest.dto.ExchangeRateDto;
import com.example.privattest.model.Currency;
import com.example.privattest.service.ExchangeRateService;
import com.example.privattest.validation.AllowedCurrency;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange-rate")
@RequiredArgsConstructor
@Tag(
        name = "Currency information controller",
        description = "Endpoints for getting information about currency rates"
)
@Slf4j
@Validated
public class ExchangeRateController {
    private final ExchangeRateService exchangeRateService;

    /**
     * Retrieves the latest exchange rate for the specified currency.
     *
     * <p><i>
     * If no data is found, it returns a successful status 200 (Ok) with error detailed message.
     * This is considered as normal behavior when no information is available
     * for the requested currency.
     * </i></p>
     *
     * @param currency the currency code
     * @return the latest exchange rate for the specified currency
     */
    @Operation(summary = "Get the latest exchange rate",
            description = "Fetches the latest exchange rate for the specified currency.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved the latest exchange rate",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(oneOf = {
                                    ExchangeRateDto.class,
                                    ErrorResponseDto.class
                            }))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @GetMapping("/latest")
    public ExchangeRateDto getLatestRate(
            @RequestParam
            @Parameter(
                    description = "Currency code",
                    schema = @Schema(type = "string", allowableValues = {"USD", "EUR"})
            )
            @AllowedCurrency Currency currency) {
        return exchangeRateService.getLatestRate(currency);
    }

    /**
     * Retrieves the exchange rate change for the last hour.
     *
     * <p><i>
     * If no data is found, it returns a successful status 200 (Ok) with error detailed message.
     * This is considered as normal behavior when no information is available
     * for the requested currency.
     * </i></p>
     *
     * @param currency the currency code
     * @return the exchange rate percentage change details or an error message
     * if data is unavailable
     */
    @Operation(
            summary = "Get hourly exchange rate difference",
            description = """
                    Fetches the exchange rate percentage change over the last hour\s
                    for the specified currency.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Successfully retrieved hourly exchange rate change or no data available
                            """,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(oneOf = {
                                    DynamicDetailsDto.class,
                                    ErrorResponseDto.class
                            }))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @GetMapping("/hourly-difference")
    public DynamicDetailsDto getHourlyDifference(
            @RequestParam
            @Parameter(
                    description = "Currency code",
                    schema = @Schema(type = "string", allowableValues = {"USD", "EUR"})
            )
            @AllowedCurrency Currency currency
    ) {
        return exchangeRateService.getHourlyDynamics(currency);
    }

    /**
     * Retrieves hourly exchange rate dynamics for the current day.
     *
     * @param currency the currency code
     * @return a list of hourly percentage changes in exchange rates
     * or an error message if data is unavailable
     */
    @Operation(
            summary = "Get daily hourly exchange rate dynamics",
            description = """
                    Fetches the hourly percentage changes in exchange rates\s
                    for the specified currency throughout the current day.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved daily hourly exchange rate dynamics",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(
                                    implementation = DynamicDetailsDto.class,
                                    type = "array"
                            ))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @GetMapping("/daily-dynamics")
    public List<DynamicDetailsDto> getDailyDynamics(
            @RequestParam
            @Parameter(
                    description = "Currency code",
                    schema = @Schema(type = "string", allowableValues = {"USD", "EUR"})
            )
            @AllowedCurrency Currency currency) {
        return exchangeRateService.getDailyDynamics(currency);
    }
}
