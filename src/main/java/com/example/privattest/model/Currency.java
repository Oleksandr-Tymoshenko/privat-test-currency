package com.example.privattest.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum Currency {
    USD("840"),
    EUR("978"),
    UAH("980");

    private final String numericCode;

    Currency(String numericCode) {
        this.numericCode = numericCode;
    }

    public Integer getIntegerNumericCode() {
        return Integer.parseInt(numericCode);
    }

    /**
     * Converts a numeric currency code to a corresponding {@link Currency} enumeration value.
     *
     * @param code the numeric code of the currency (e.g., 840 for USD, 978 for EUR)
     * @return the {@link Currency} enumeration value corresponding to the provided code,
     * or {@code null} if no matching currency is found
     */
    public static Currency fromCode(Integer code) {
        for (Currency currency : values()) {
            if (currency.getNumericCode().equalsIgnoreCase(code.toString())) {
                return currency;
            }
        }
        log.debug("No currency found for code: " + code);
        return null;
    }
}
