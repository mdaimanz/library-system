package com.library.management.api.dto;

import java.util.UUID;

public record BookResponse(
        UUID id,
        String isbnNumber,
        String title,
        String author
) {
}
