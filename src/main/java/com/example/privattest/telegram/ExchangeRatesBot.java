package com.example.privattest.telegram;

import com.example.privattest.model.ExchangeRate;
import com.example.privattest.service.UserChatIdService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * A bot class that interacts with users to send currency exchange rates via Telegram.
 * The bot listens for commands and sends updated exchange rate information to users.
 */
@Component
@Slf4j
public class ExchangeRatesBot extends TelegramLongPollingBot {
    private static final String START_COMMAND = "/start";
    private final UserChatIdService userChatIdService;

    @Value("${telegram.bot.name}")
    private String botName;

    public ExchangeRatesBot(@Value("${telegram.bot.token}") String botToken,
                            @Autowired UserChatIdService userChatIdService) {
        super(botToken);
        this.userChatIdService = userChatIdService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getChat().getUserName();
        log.info("Received message from user: {} with chat ID: {}. Message: {}",
                userName, chatId, message);

        // Save user chat ID to the database
        userChatIdService.saveChatId(chatId, userName);

        switch (message) {
            case START_COMMAND -> startCommand(chatId, userName);
            default -> defaultCommand(chatId);
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    /**
     * Sends a message to a specific user on Telegram.
     *
     * @param chatId the unique chat ID of the user
     * @param text   the text message to be sent to the user
     */
    public void sendMessage(Long chatId, String text) {
        String chatIdString = String.valueOf(chatId);
        SendMessage sendMessage = new SendMessage(chatIdString, text);
        try {
            execute(sendMessage);
            log.info("Successfully sent message to chat ID: {}. Message: {}", chatId, text);
        } catch (TelegramApiException ex) {
            log.error("Couldn't send message in telegram. Message: {}. Exception: {}",
                    text, ex.getMessage());
        }
    }

    /**
     * Sends the updated exchange rates to the user.
     *
     * @param chatId        the unique chat ID of the user
     * @param exchangeRates a list of exchange rates to send
     */
    public void sendCurrencyData(Long chatId, List<ExchangeRate> exchangeRates) {
        String text = """
                Оновлені курси валют:
                %s
                """;
        // Format the exchange rates as a string
        String currencyDataString = exchangeRates.stream()
                .map(rate -> String.format("%s \n Покупка: %s, Продаж: %s",
                        rate.getCurrency().name(),
                        rate.getRateBuy(),
                        rate.getRateSell())
                ).collect(Collectors.joining("\n"));

        sendMessage(chatId, String.format(text, currencyDataString));
    }

    /**
     * Sends a welcome message to a user when they start the bot.
     *
     * @param chatId   the unique chat ID of the user
     * @param userName the username of the user
     */
    private void startCommand(Long chatId, String userName) {
        String text = """
                Привіт, %s!
                В цьому боті ти будеш отримувати повідомлення про актуальний курс валют
                 """;
        sendMessage(chatId, String.format(text, userName));
    }

    /**
     * Sends a default message when the user sends an unrecognized command.
     *
     * @param chatId the unique chat ID of the user
     */
    private void defaultCommand(Long chatId) {
        String text = """
                Ця команда не розпізнана!
                """;
        sendMessage(chatId, text);
    }
}
