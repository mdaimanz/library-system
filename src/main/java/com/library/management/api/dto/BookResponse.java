package com.library.management.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "A book in the library catalogue")
public record BookResponse(
        @Schema(description = "Unique book identifier", example = "7b7d1847-34b2-4cbd-a7e4-601a967446b0", format = "uuid") UUID id,
        @Schema(description = "Book ISBN", example = "9780306406157") String isbnNumber,
        @Schema(description = "Book title", example = "The Pragmatic Programmer") String title,
        @Schema(description = "Book author", example = "David Thomas") String author
) {
}
