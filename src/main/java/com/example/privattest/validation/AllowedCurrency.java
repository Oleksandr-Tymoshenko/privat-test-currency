package com.example.privattest.validation;

import com.example.privattest.model.Currency;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for ensuring that a provided currency value
 * is one of the allowed values (e.g., USD, EUR).
 * <p>
 * This annotation can be applied to method parameters to validate their values
 * against a predefined set of allowed currencies.
 * </p>
 *
 * <p>Usage example:</p>
 * <pre>
 * public void processCurrency(@AllowedCurrency Currency currency) {
 *     // Method implementation
 * }
 * </pre>
 *
 * <p>The actual validation logic is implemented in the {@link AllowedCurrencyValidator} class.</p>
 */
@Constraint(validatedBy = AllowedCurrencyValidator.class)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedCurrency {
    String message() default "Invalid currency. Allowed values: USD, EUR";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Specifies the allowed currency values for validation.
     *
     * @return an array of allowed currency values
     */
    Currency[] allowedValues() default {Currency.USD, Currency.EUR};
}
