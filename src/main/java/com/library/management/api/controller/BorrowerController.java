package com.library.management.api.controller;

import com.library.management.api.dto.BorrowerRequest;
import com.library.management.api.dto.BorrowerResponse;
import com.library.management.service.BorrowerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/borrowers")
@RequiredArgsConstructor
@Tag(name = "Borrowers", description = "Register and manage library borrowers")
public class BorrowerController {

    private final BorrowerService borrowerService;

    @PostMapping
    @Operation(
            summary = "Register a borrower",
            description = "Registers a borrower with a unique email address. Names may contain letters and spaces."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Borrower registered",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BorrowerResponse.class),
                            examples = @ExampleObject(value = """
                                    {"id":"458015b3-8688-4b03-b6df-5579fe6e1296","name":"Ada Lovelace","emailAddress":"ada@example.com"}
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request, invalid name or email, or duplicate email",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = {
                                    @ExampleObject(name = "invalidName", value = """
                                            {"type":"about:blank","title":"Invalid borrower name","status":400,"detail":"Name must contain only letters and spaces","instance":"/api/v1/borrowers"}
                                            """),
                                    @ExampleObject(name = "invalidEmail", value = """
                                            {"type":"about:blank","title":"Invalid email format","status":400,"detail":"Invalid email format","instance":"/api/v1/borrowers"}
                                            """),
                                    @ExampleObject(name = "duplicateEmail", value = """
                                            {"type":"about:blank","title":"Duplicate email","status":400,"detail":"Email has been used","instance":"/api/v1/borrowers"}
                                            """)
                            }
                    )
            )
    })
    public ResponseEntity<BorrowerResponse> create(@Valid @RequestBody BorrowerRequest request) {
        BorrowerResponse response = borrowerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
