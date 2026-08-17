package com.library.management.api.dto;

import jakarta.validation.constraints.NotBlank;

public record BookRequest(
        @NotBlank String isbnNumber,
        @NotBlank String title,
        @NotBlank String author
){
}
