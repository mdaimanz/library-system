package com.library.management.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReturnBookRequest(
        @NotNull UUID borrowerId
){
}
