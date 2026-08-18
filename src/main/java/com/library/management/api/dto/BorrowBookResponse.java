package com.library.management.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of a request to borrow a book")
public record BorrowBookResponse(
        @Schema(description = "Business outcome", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILED"}) String status,
        @Schema(description = "Human-readable outcome description", example = "Book 7b7d1847-34b2-4cbd-a7e4-601a967446b0 is now borrowed by 458015b3-8688-4b03-b6df-5579fe6e1296") String description
){
}
