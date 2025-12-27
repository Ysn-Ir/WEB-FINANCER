package com.smartfin.backend.controller;

import com.smartfin.backend.service.ForecastingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/forecast")
@CrossOrigin(origins = "http://localhost:4200")
public class ForecastingController {

    @Autowired
    private ForecastingService forecastingService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getForecast(
            @RequestParam(defaultValue = "10") int years,
            @RequestParam(defaultValue = "7.0") double annualReturnRate,
            @RequestParam(required = false) Double monthlyContribution,
            java.security.Principal principal) {
        return ResponseEntity.ok(
                forecastingService.generateForecast(principal.getName(), years, annualReturnRate, monthlyContribution));
    }
}
