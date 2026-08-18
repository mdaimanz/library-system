package com.library.management.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Borrower requesting to return the book")
public record ReturnBookRequest(
        @Schema(description = "Unique borrower identifier", example = "458015b3-8688-4b03-b6df-5579fe6e1296", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID borrowerId
){
}
