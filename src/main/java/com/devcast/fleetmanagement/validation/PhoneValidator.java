package com.devcast.fleetmanagement.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneValidator implements ConstraintValidator<Phone, String> {

    // Example: Ethiopian + international formats
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+251|0)?9\\d{8}$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false; // or true if optional
        }
        return PHONE_PATTERN.matcher(value).matches();
    }
}
