package com.library.management.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/borrowers")
@RequiredArgsConstructor
public class BorrowerController {


    @GetMapping
    public String test() { return "Hello this is Gustavo Fring";};
}
