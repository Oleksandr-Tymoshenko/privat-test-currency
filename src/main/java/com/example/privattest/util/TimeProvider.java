package com.example.privattest.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * A utility class for providing the current date and time.
 * <p>
 * This class acts as a wrapper around the standard Java time utilities,
 * allowing for easier mocking and testing of time-related logic in applications.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * TimeProvider timeProvider = new TimeProvider();
 * LocalDateTime currentTime = timeProvider.now();
 * LocalDate currentDate = timeProvider.today();
 * }
 * </pre>
 *
 * <p>By using this class, you can decouple time-dependent code from
 * direct calls to {@code LocalDateTime.now()} and {@code LocalDate.now()},
 * making it easier to write unit tests.</p>
 */
@Component
public class TimeProvider {
    /**
     * Returns the current date and time.
     * <p>
     * This method provides the current {@link LocalDateTime} based on the system's
     * default time zone and clock.
     * </p>
     *
     * @return the current date and time as a {@link LocalDateTime} instance
     */
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Returns the current date.
     * <p>
     * This method provides the current {@link LocalDate} based on the system's
     * default time zone and clock.
     * </p>
     *
     * @return the current date as a {@link LocalDate} instance
     */
    public LocalDate today() {
        return LocalDate.now();
    }
}
