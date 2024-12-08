package com.example.privattest.dto;

import com.example.privattest.model.Currency;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DynamicDetailsDto(
        Currency currency,
        BigDecimal percentageChangeBuy,
        LocalDateTime oldRateTimestamp,
        BigDecimal percentageChangeSell,
        LocalDateTime newRateTimestamp
) {
}
