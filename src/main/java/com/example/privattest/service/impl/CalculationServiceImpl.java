package com.example.privattest.service.impl;

import com.example.privattest.dto.DynamicDetailsDto;
import com.example.privattest.model.Currency;
import com.example.privattest.model.ExchangeRate;
import com.example.privattest.service.CalculationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CalculationServiceImpl implements CalculationService {
    @Value("${max.minutes.difference-between-rates}")
    private Long maxDifference;

    @Override
    public DynamicDetailsDto calculateDynamicDetails(
            Currency currency, ExchangeRate oldRate, ExchangeRate latestRate) {
        BigDecimal percentageChangeBuy = calculatePercentageChange(
                oldRate.getRateBuy(), latestRate.getRateBuy()
        );
        BigDecimal percentageChangeSell = calculatePercentageChange(
                oldRate.getRateSell(), latestRate.getRateSell()
        );
        return new DynamicDetailsDto(
                currency,
                percentageChangeBuy,
                oldRate.getTimestamp(),
                percentageChangeSell,
                latestRate.getTimestamp()
        );
    }

    @Override
    public List<DynamicDetailsDto> calculateDailyDynamics(
            Currency currency, List<ExchangeRate> exchangeRates) {
        List<DynamicDetailsDto> rateChanges = new ArrayList<>();
        ExchangeRate newerRate = null;

        for (ExchangeRate currentRate : exchangeRates) {
            if (newerRate != null) {
                long timeDifferenceInMinutes = ChronoUnit.MINUTES.between(
                        newerRate.getTimestamp(),
                        currentRate.getTimestamp()
                );

                // Compare timestamps so that the time difference is less than a given value
                if (timeDifferenceInMinutes < maxDifference) {
                    DynamicDetailsDto dynamicDetailsDto = calculateDynamicDetails(
                            currency, currentRate, newerRate
                    );
                    rateChanges.add(dynamicDetailsDto);
                }
            }
            // Update previous rate for the next iteration
            newerRate = currentRate;
        }

        return rateChanges;
    }

    @Override
    public Map<Currency, ExchangeRate> calculateAverageRates(List<ExchangeRate> rates) {
        return rates.stream()
                .collect(Collectors.groupingBy(
                        ExchangeRate::getCurrency, // Group by currency
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    BigDecimal avgBuy = calculateAverage(
                                            list, ExchangeRate::getRateBuy
                                    );
                                    BigDecimal avgSell = calculateAverage(
                                            list, ExchangeRate::getRateSell
                                    );

                                    ExchangeRate averageRate = new ExchangeRate();
                                    averageRate.setCurrency(list.get(0).getCurrency());
                                    averageRate.setRateBuy(avgBuy);
                                    averageRate.setRateSell(avgSell);
                                    return averageRate;
                                }
                        )
                ));
    }

    /**
     * Calculates percentage change between old and new values.
     *
     * @param oldValue the old value
     * @param newValue the new value
     * @return percentage change
     */
    private BigDecimal calculatePercentageChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.compareTo(BigDecimal.ZERO) == 0) {
            if (newValue.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.valueOf(0);
            } else {
                // it is logical to consider the change from 0 to any number as 100%,
                // since this is the maximum growth
                return BigDecimal.valueOf(100);
            }
        }
        return newValue.subtract(oldValue)
                .divide(oldValue, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculates the average value of a specific rate (buy or sell)
     * from a list of ExchangeRate objects.
     *
     * @param rates         The list of ExchangeRate objects containing the rates.
     * @param rateExtractor A function to extract the rate (buy or sell) from an ExchangeRate.
     * @return The average value of the specified rate.
     */
    private BigDecimal calculateAverage(
            List<ExchangeRate> rates,
            Function<ExchangeRate, BigDecimal> rateExtractor) {
        return rates.stream()
                // Extract the specific rate (buy or sell) from each ExchangeRate
                .map(rateExtractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(rates.size()), 2, RoundingMode.HALF_UP);
    }
}
