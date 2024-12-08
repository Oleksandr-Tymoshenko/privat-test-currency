package com.example.privattest.dto;

import com.example.privattest.model.Currency;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExchangeRateDto(
        Currency currency,
        BigDecimal rateBuy,
        BigDecimal rateSell,
        LocalDateTime timestamp
) {
}
