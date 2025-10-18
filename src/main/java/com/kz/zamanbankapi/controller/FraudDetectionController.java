package com.kz.zamanbankapi.controller;

import com.kz.zamanbankapi.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fraud-check")
@RequiredArgsConstructor
public class FraudDetectionController {
    private final FraudDetectionService fraudDetectionService;
    @GetMapping
    public boolean getFraud(@RequestParam String phoneNumber) {
        return fraudDetectionService.getFraud(phoneNumber);
    }
}
