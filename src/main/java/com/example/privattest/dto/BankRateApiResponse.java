package com.example.privattest.dto;

import com.example.privattest.model.Currency;
import java.math.BigDecimal;

public interface BankRateApiResponse {
    Currency getCurrency();

    BigDecimal getRateBuy();

    BigDecimal getRateSell();
}
