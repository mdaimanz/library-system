package com.library.management.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of a request to return a book")
public record ReturnBookResponse(
        @Schema(description = "Business outcome", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILED"}) String status,
        @Schema(description = "Human-readable outcome description", example = "Book 7b7d1847-34b2-4cbd-a7e4-601a967446b0 is successfully returned") String description
) {
}
