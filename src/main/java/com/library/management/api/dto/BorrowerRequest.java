package com.library.management.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record BorrowerRequest(
    @NotBlank String name,
    @NotBlank @Email String email
) {
}
