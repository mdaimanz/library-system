package com.library.management.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BorrowBookRequest(
        @NotNull UUID borrowerId
){
}
