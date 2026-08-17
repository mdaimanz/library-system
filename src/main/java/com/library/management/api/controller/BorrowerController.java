package com.library.management.api.controller;

import com.library.management.api.dto.BorrowerRequest;
import com.library.management.api.dto.BorrowerResponse;
import com.library.management.service.BorrowerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/borrowers")
@RequiredArgsConstructor
public class BorrowerController {

    private final BorrowerService borrowerService;

    @PostMapping
    public ResponseEntity<BorrowerResponse> create(@Valid @RequestBody BorrowerRequest request) {
        BorrowerResponse response = borrowerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
