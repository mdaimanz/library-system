package com.library.management.api.controller;

import com.library.management.api.dto.BorrowerRequest;
import com.library.management.api.dto.BorrowerResponse;
import com.library.management.exception.DuplicateEmailException;
import com.library.management.exception.InvalidEmailFormatException;
import com.library.management.exception.InvalidNameException;
import com.library.management.service.BorrowerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BorrowerController.class)
class BorrowerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BorrowerService borrowerService;

    @Test
    void createReturnsCreatedBorrowerAndDelegatesRequest() throws Exception {
        BorrowerRequest request = new BorrowerRequest("Valid Borrower", "borrower@example.com");
        BorrowerResponse response = new BorrowerResponse(UUID.randomUUID(), request.name(), request.email());
        when(borrowerService.create(any(BorrowerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.name").value(response.name()))
                .andExpect(jsonPath("$.emailAddress").value(response.emailAddress()));

        verify(borrowerService).create(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"name\":\"\",\"email\":\"borrower@example.com\"}",
            "{\"name\":\"Valid Borrower\",\"email\":\"\"}",
            "{\"name\":\"Valid Borrower\",\"email\":\"not-an-email\"}"
    })
    void createRejectsInvalidRequestWithoutCallingService(String body) throws Exception {
        mockMvc.perform(post("/api/v1/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(borrowerService);
    }

    @Test
    void createMapsInvalidNameToProblemDetail() throws Exception {
        BorrowerRequest request = new BorrowerRequest("Borrower 123", "borrower@example.com");
        when(borrowerService.create(any(BorrowerRequest.class)))
                .thenThrow(new InvalidNameException("Name must contain only letters and spaces"));

        performCreate(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Invalid borrower name"))
                .andExpect(jsonPath("$.detail").value("Name must contain only letters and spaces"));
    }

    @Test
    void createMapsInvalidEmailFormatToProblemDetail() throws Exception {
        BorrowerRequest request = new BorrowerRequest("Valid Borrower", "borrower@example.com");
        when(borrowerService.create(any(BorrowerRequest.class)))
                .thenThrow(new InvalidEmailFormatException("Invalid email format"));

        performCreate(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Invalid email format"))
                .andExpect(jsonPath("$.detail").value("Invalid email format"));
    }

    @Test
    void createMapsDuplicateEmailToProblemDetail() throws Exception {
        BorrowerRequest request = new BorrowerRequest("Valid Borrower", "borrower@example.com");
        when(borrowerService.create(any(BorrowerRequest.class)))
                .thenThrow(new DuplicateEmailException("Email has been used"));

        performCreate(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Duplicate email"))
                .andExpect(jsonPath("$.detail").value("Email has been used"));
    }

    private org.springframework.test.web.servlet.ResultActions performCreate(BorrowerRequest request) throws Exception {
        return mockMvc.perform(post("/api/v1/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
