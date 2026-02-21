package com.piyushjain.resilience4jpoc.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/external")
public class ExternalApiController {

    @GetMapping("/data")
    public String getData() throws InterruptedException {

        // Simulate network delay
        Thread.sleep(800);

        // 70% failure rate
        if (Math.random() < 0.7) {
            throw new RuntimeException("External API Failed");
        }

        return "External API Success";
    }
}