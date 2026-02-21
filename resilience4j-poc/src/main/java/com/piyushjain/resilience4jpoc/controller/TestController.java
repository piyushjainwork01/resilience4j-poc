package com.piyushjain.resilience4jpoc.controller;



import com.piyushjain.resilience4jpoc.service.ExternalApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final ExternalApiService service;

    @GetMapping
    public String testCall() {
        return service.callExternalApi();
    }
}