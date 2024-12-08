package com.example.privattest.notification;

import com.example.privattest.model.ExchangeRate;
import java.util.List;

/**
 * Service interface for sending notifications about currency exchange rates.
 * Implementations of this interface are responsible for notifying users or other systems
 * about updated exchange rates.
 */
public interface NotificationService {
    /**
     * Sends notifications with the provided list of exchange rates.
     *
     * @param exchangeRates a list of {@link ExchangeRate} objects representing
     *                      the current exchange rates
     *                      to be sent as notifications.
     *                      The list may contain different currencies and their respective
     *                      buy and sell rates.
     */
    void notify(List<ExchangeRate> exchangeRates);
}
