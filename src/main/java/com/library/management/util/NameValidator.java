package com.library.management.util;

import com.library.management.exception.InvalidNameException;

import java.util.regex.Pattern;

public class NameValidator {

    private static final Pattern VALID_NAME_PATTERN =
            Pattern.compile("^\\p{L}[\\p{L}\\p{M}]*(?: \\p{L}[\\p{L}\\p{M}]*)*$");

    public static void validateName(String name) {
        if (name == null || name.isBlank() || !VALID_NAME_PATTERN.matcher(name).matches()) {
            throw new InvalidNameException("Name must contain only letters and spaces");
        }
    }
}
