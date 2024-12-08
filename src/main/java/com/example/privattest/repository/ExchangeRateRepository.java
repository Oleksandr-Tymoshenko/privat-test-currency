package com.example.privattest.repository;

import com.example.privattest.model.Currency;
import com.example.privattest.model.ExchangeRate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    Optional<ExchangeRate> findTopByCurrencyOrderByTimestampDesc(Currency currency);

    Optional<ExchangeRate> findTopByCurrencyAndTimestampBetweenOrderByTimestampDesc(
            Currency currency, LocalDateTime startTime, LocalDateTime endTime
    );

    List<ExchangeRate> findAllByCurrencyAndTimestampBetweenOrderByTimestampDesc(
            Currency currency, LocalDateTime startDate, LocalDateTime endTime
    );
}
