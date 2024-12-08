package com.example.privattest.dto;

import com.example.privattest.model.Currency;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonoRateApiResponse implements BankRateApiResponse {
    private Integer currencyCodeA;
    private Integer currencyCodeB;
    private long date;
    private double rateCross;
    private BigDecimal rateSell;
    private BigDecimal rateBuy;

    @Override
    public Currency getCurrency() {
        if (Currency.UAH.equals(Currency.fromCode(currencyCodeB))) {
            return Currency.fromCode(currencyCodeA);
        }
        return null;
    }
}
