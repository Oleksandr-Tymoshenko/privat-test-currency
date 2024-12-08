package com.example.privattest.notification.impl;

import com.example.privattest.model.ExchangeRate;
import com.example.privattest.notification.NotificationService;
import com.example.privattest.service.UserChatIdService;
import com.example.privattest.telegram.ExchangeRatesBot;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationService implements NotificationService {
    private final ExchangeRatesBot exchangeRatesBot;
    private final UserChatIdService userChatIdService;

    @Override
    public void notify(List<ExchangeRate> exchangeRates) {
        log.info("Starting notification process for {} exchange rates.", exchangeRates.size());

        CompletableFuture.runAsync(() -> {
            try {
                userChatIdService.getUsersChatIds().forEach(chatId -> {
                    log.info("Sending currency data to chatId: {}", chatId.getChatId());
                    exchangeRatesBot.sendCurrencyData(chatId.getChatId(), exchangeRates);
                });
            } catch (Exception e) {
                log.error("Error during the notification process: {}", e.getMessage(), e);
            }
        });
    }
}
