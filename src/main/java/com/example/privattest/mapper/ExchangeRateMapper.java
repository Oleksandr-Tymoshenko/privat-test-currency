package com.example.privattest.mapper;

import com.example.privattest.dto.BankRateApiResponse;
import com.example.privattest.dto.ExchangeRateDto;
import com.example.privattest.model.ExchangeRate;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface ExchangeRateMapper {
    ExchangeRate toExchangeRate(BankRateApiResponse bankRateApiResponse);

    ExchangeRateDto toDto(ExchangeRate exchangeRate);
}
