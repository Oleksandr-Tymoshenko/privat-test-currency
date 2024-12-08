package com.example.privattest.exception;

public class CurrencyDataNotFoundException extends RuntimeException {
    public CurrencyDataNotFoundException(String request) {
        super(String.format(
                "No information found for the currency you requested: \"%s\". Please try later",
                request));
    }
}
