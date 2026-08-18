package com.library.management.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Details required to add a book to the catalogue")
public record BookRequest(
        @Schema(description = "Valid ISBN-10 or ISBN-13; spaces and hyphens are accepted", example = "9780306406157", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String isbnNumber,
        @Schema(description = "Book title", example = "The Pragmatic Programmer", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String title,
        @Schema(description = "Author name containing letters and spaces", example = "David Thomas", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String author
){
}
