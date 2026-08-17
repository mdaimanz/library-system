package com.library.management.api.dto;

import java.util.UUID;

public record BorrowerResponse (
    UUID id,
    String name,
    String emailAddress
) {
}
