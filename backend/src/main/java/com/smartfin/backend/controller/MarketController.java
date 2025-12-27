package com.smartfin.backend.controller;

import com.smartfin.backend.model.PriceHistory;

import com.smartfin.backend.service.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "http://localhost:4200")
public class MarketController {

    @Autowired
    private MarketDataService marketDataService;

    @GetMapping("/history/{symbol}")
    public List<PriceHistory> getHistory(@PathVariable String symbol,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) BigDecimal referencePrice) {
        // Default period to 1D if null
        String p = (period != null) ? period : "1D";
        return marketDataService.getHistoricalData(symbol, p, referencePrice);
    }

    @GetMapping("/price/{symbol}")
    public BigDecimal getPrice(@PathVariable String symbol, @RequestParam(defaultValue = "STOCK") String type) {
        return marketDataService.getPrice(symbol, type);
    }

    @GetMapping("/search")
    public List<Map<String, String>> searchAssets(@RequestParam String query,
            @RequestParam(required = false) String type) {
        String t = (type != null) ? type : "ALL";
        return marketDataService.searchAssets(query, t);
    }
}
