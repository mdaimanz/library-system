package com.library.management.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "A registered library borrower")
public record BorrowerResponse (
    @Schema(description = "Unique borrower identifier", example = "458015b3-8688-4b03-b6df-5579fe6e1296", format = "uuid") UUID id,
    @Schema(description = "Borrower name", example = "Ada Lovelace") String name,
    @Schema(description = "Borrower email address", example = "ada@example.com", format = "email") String emailAddress
) {
}
