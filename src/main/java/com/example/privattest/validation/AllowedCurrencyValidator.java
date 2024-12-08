package com.example.privattest.validation;

import com.example.privattest.model.Currency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class AllowedCurrencyValidator implements ConstraintValidator<AllowedCurrency, Currency> {

    private Currency[] allowedValues;

    @Override
    public void initialize(AllowedCurrency constraintAnnotation) {
        this.allowedValues = constraintAnnotation.allowedValues();
    }

    @Override
    public boolean isValid(Currency value, ConstraintValidatorContext context) {
        return value != null && Arrays.asList(allowedValues).contains(value);
    }
}
