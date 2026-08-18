package com.library.management.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Details required to register a borrower")
public record BorrowerRequest(
    @Schema(description = "Borrower name containing letters and spaces", example = "Ada Lovelace", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank String name,
    @Schema(description = "Unique, valid email address", example = "ada@example.com", format = "email", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank @Email String email
) {
}
