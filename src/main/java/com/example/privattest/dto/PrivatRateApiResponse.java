package com.example.privattest.dto;

import com.example.privattest.model.Currency;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivatRateApiResponse implements BankRateApiResponse {
    private Currency ccy;
    @JsonProperty("base_ccy")
    private Currency baseCcy;
    private String buy;
    private String sale;

    @Override
    public Currency getCurrency() {
        return ccy;
    }

    @Override
    public BigDecimal getRateBuy() {
        return new BigDecimal(buy);
    }

    @Override
    public BigDecimal getRateSell() {
        return new BigDecimal(sale);
    }
}
