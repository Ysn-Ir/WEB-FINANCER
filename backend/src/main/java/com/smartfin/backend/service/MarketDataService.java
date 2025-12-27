package com.smartfin.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class MarketDataService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String ALPHA_VANTAGE_URL = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol={symbol}&apikey={apiKey}";
    private static final String COINGECKO_URL = "https://api.coingecko.com/api/v3/simple/price?ids={id}&vs_currencies=usd&x_cg_demo_api_key={apiKey}";

    // Simulating prices if API fails or for demo
    private final Random random = new Random();

    public BigDecimal getPrice(String symbol, String type) {
        try {
            if ("CRYPTO".equalsIgnoreCase(type)) {
                return fetchCryptoPrice(symbol);
            } else {
                return fetchStockPrice(symbol);
            }
        } catch (Exception e) {
            System.err.println("Market API failed for " + symbol + ": " + e.getMessage());
            return simulatePrice(symbol);
        }
    }

    private BigDecimal fetchStockPrice(String symbol) {
        Map<String, String> params = new HashMap<>();
        params.put("symbol", symbol);
        // User provided Alpha Vantage Key
        params.put("apiKey", "ONYKAI1LKO6PW7TN");

        Map response = restTemplate.getForObject(ALPHA_VANTAGE_URL, Map.class, params);
        if (response != null && response.containsKey("Global Quote")) {
            Map quote = (Map) response.get("Global Quote");
            String priceStr = (String) quote.get("05. price");
            if (priceStr != null) {
                return new BigDecimal(priceStr);
            }
        }
        throw new RuntimeException("No data found for " + symbol);
    }

    private BigDecimal fetchCryptoPrice(String symbol) {
        // CoinGecko requires ID (e.g., 'bitcoin'), not symbol ('BTC').
        String id = symbol.toLowerCase();
        if (id.equals("btc"))
            id = "bitcoin";
        if (id.equals("eth"))
            id = "ethereum";
        if (id.equals("sol"))
            id = "solana";

        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        // User provided CoinGecko Key
        params.put("apiKey", "CG-Z5RFSn244NtXg1FmsX9aWHmG");

        // Note: URL template handles the key replacement
        Map response = restTemplate.getForObject(COINGECKO_URL, Map.class, params);

        if (response != null && response.containsKey(id)) {
            Map data = (Map) response.get(id);
            Object priceObj = data.get("usd");
            if (priceObj != null) {
                return new BigDecimal(priceObj.toString());
            }
        }
        throw new RuntimeException("No data found for " + symbol);
    }

    private static final String ALPHA_VANTAGE_HISTORY_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol={symbol}&apikey={apiKey}";

    private static final String ALPHA_VANTAGE_SEARCH_URL = "https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords={keywords}&apikey={apiKey}";

    public java.util.List<Map<String, String>> searchAssets(String keywords, String type) {
        java.util.List<Map<String, String>> results = new java.util.ArrayList<>();
        try {
            Map<String, String> params = new HashMap<>();
            params.put("keywords", keywords);
            params.put("apiKey", "ONYKAI1LKO6PW7TN");

            Map response = restTemplate.getForObject(ALPHA_VANTAGE_SEARCH_URL, Map.class, params);

            if (response != null && response.containsKey("bestMatches")) {
                java.util.List<Map<String, String>> matches = (java.util.List<Map<String, String>>) response
                        .get("bestMatches");
                for (Map<String, String> match : matches) {
                    // Filter by type logic here...
                    String matchType = match.get("3. type");
                    boolean include = true;
                    if ("STOCK".equalsIgnoreCase(type)) {
                        include = "Equity".equalsIgnoreCase(matchType) || "ETF".equalsIgnoreCase(matchType);
                    } else if ("CRYPTO".equalsIgnoreCase(type)) {
                        include = "Currency".equalsIgnoreCase(matchType)
                                || "Digital Currency".equalsIgnoreCase(matchType);
                    }

                    if (include) {
                        Map<String, String> asset = new HashMap<>();
                        asset.put("symbol", match.get("1. symbol"));
                        asset.put("name", match.get("2. name"));
                        asset.put("type", "Equity".equalsIgnoreCase(matchType) ? "STOCK" : "CRYPTO");
                        results.add(asset);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to search assets: " + e.getMessage());
        }

        if (results.isEmpty()) {
            results = getFallbackAssets(keywords, type);
        }
        return results;
    }

    private java.util.List<Map<String, String>> getFallbackAssets(String query, String type) {
        String q = query.toUpperCase();
        return java.util.List.of(
                Map.of("symbol", "BTC", "name", "Bitcoin", "type", "CRYPTO"),
                Map.of("symbol", "ETH", "name", "Ethereum", "type", "CRYPTO"),
                Map.of("symbol", "SOL", "name", "Solana", "type", "CRYPTO"),
                Map.of("symbol", "XRP", "name", "Ripple", "type", "CRYPTO"),
                Map.of("symbol", "ADA", "name", "Cardano", "type", "CRYPTO"),
                Map.of("symbol", "DOGE", "name", "Dogecoin", "type", "CRYPTO"),
                Map.of("symbol", "DOT", "name", "Polkadot", "type", "CRYPTO"),
                Map.of("symbol", "MATIC", "name", "Polygon", "type", "CRYPTO"),
                Map.of("symbol", "LINK", "name", "Chainlink", "type", "CRYPTO"),
                Map.of("symbol", "AVAX", "name", "Avalanche", "type", "CRYPTO"),
                Map.of("symbol", "AAPL", "name", "Apple Inc.", "type", "STOCK"),
                Map.of("symbol", "TSLA", "name", "Tesla Inc.", "type", "STOCK"),
                Map.of("symbol", "GOOGL", "name", "Alphabet Inc.", "type", "STOCK"),
                Map.of("symbol", "NVDA", "name", "NVIDIA Corp", "type", "STOCK"),
                Map.of("symbol", "MSFT", "name", "Microsoft Corp", "type", "STOCK"),
                Map.of("symbol", "AMZN", "name", "Amazon.com Inc.", "type", "STOCK")).stream()
                .filter(m -> {
                    boolean matchName = m.get("symbol").contains(q) || m.get("name").toUpperCase().contains(q);
                    boolean matchType = type == null || "ALL".equalsIgnoreCase(type)
                            || type.equalsIgnoreCase(m.get("type"));
                    return matchName && matchType;
                })
                .toList();
    }

    private static final String ALPHA_VANTAGE_INTRADAY_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol={symbol}&interval=60min&apikey={apiKey}";
    private static final String ALPHA_VANTAGE_INTRADAY_5MIN_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol={symbol}&interval=5min&apikey={apiKey}";

    public java.util.List<com.smartfin.backend.model.PriceHistory> getHistoricalData(String symbol, String period,
            BigDecimal referencePrice) {
        // period: "1H" (5min), "1D" (60min, default), "7D" (Daily)

        try {
            Map<String, String> params = new HashMap<>();
            params.put("symbol", symbol);
            params.put("apiKey", "ONYKAI1LKO6PW7TN");

            if ("7D".equalsIgnoreCase(period)) {
                // --- 7 DAYS (DAILY) ---
                Map response = restTemplate.getForObject(ALPHA_VANTAGE_HISTORY_URL, Map.class, params);
                if (response != null && response.containsKey("Time Series (Daily)")) {
                    Map<String, Map<String, String>> timeSeries = (Map<String, Map<String, String>>) response
                            .get("Time Series (Daily)");
                    java.util.List<com.smartfin.backend.model.PriceHistory> list = new java.util.ArrayList<>();
                    timeSeries.entrySet().stream().limit(7).forEach(e -> {
                        com.smartfin.backend.model.PriceHistory ph = new com.smartfin.backend.model.PriceHistory();
                        ph.setSymbol(symbol);
                        ph.setPrice(new BigDecimal(e.getValue().get("4. close")));
                        ph.setTimestamp(java.time.LocalDate.parse(e.getKey()).atStartOfDay());
                        list.add(ph);
                    });
                    // Also save to DB for caching if we wanted
                    return list;
                }
            } else if ("1H".equalsIgnoreCase(period)) {
                // --- 1 HOUR (5 MIN Intra) ---
                Map response = restTemplate.getForObject(ALPHA_VANTAGE_INTRADAY_5MIN_URL, Map.class, params);
                if (response != null && response.containsKey("Time Series (5min)")) {
                    Map<String, Map<String, String>> timeSeries = (Map<String, Map<String, String>>) response
                            .get("Time Series (5min)");
                    java.util.List<com.smartfin.backend.model.PriceHistory> list = new java.util.ArrayList<>();
                    // key: "2023-10-25 14:35:00"
                    timeSeries.entrySet().stream().limit(12).forEach(e -> { // 12 * 5min = 60min
                        com.smartfin.backend.model.PriceHistory ph = new com.smartfin.backend.model.PriceHistory();
                        ph.setSymbol(symbol);
                        ph.setPrice(new BigDecimal(e.getValue().get("4. close")));
                        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                                .ofPattern("yyyy-MM-dd HH:mm:ss");
                        ph.setTimestamp(java.time.LocalDateTime.parse(e.getKey(), fmt));
                        list.add(ph);
                    });
                    return list;
                }
            } else {
                // --- DEFAULT: 1 DAY (60 MIN Intra) ---
                // Same logic as before
                Map response = restTemplate.getForObject(ALPHA_VANTAGE_INTRADAY_URL, Map.class, params);
                if (response != null && response.containsKey("Time Series (60min)")) {
                    Map<String, Map<String, String>> timeSeries = (Map<String, Map<String, String>>) response
                            .get("Time Series (60min)");
                    java.util.List<com.smartfin.backend.model.PriceHistory> list = new java.util.ArrayList<>();
                    timeSeries.entrySet().stream().limit(16).forEach(e -> { // approx 1-2 days opening hours
                        com.smartfin.backend.model.PriceHistory ph = new com.smartfin.backend.model.PriceHistory();
                        ph.setSymbol(symbol);
                        ph.setPrice(new BigDecimal(e.getValue().get("4. close")));
                        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                                .ofPattern("yyyy-MM-dd HH:mm:ss");
                        ph.setTimestamp(java.time.LocalDateTime.parse(e.getKey(), fmt));
                        list.add(ph);
                    });
                    return list;
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to fetch data for " + symbol + " (" + period + "): " + e.getMessage());
        }

        // Fallback
        return generateSimulatedHistory(symbol, referencePrice, period);
    }

    private java.util.List<com.smartfin.backend.model.PriceHistory> generateSimulatedHistory(String symbol,
            BigDecimal referencePrice, String period) {
        java.util.List<com.smartfin.backend.model.PriceHistory> simulated = new java.util.ArrayList<>();
        double basePrice = (referencePrice != null && referencePrice.compareTo(BigDecimal.ZERO) > 0)
                ? referencePrice.doubleValue()
                : (Math.abs(symbol.hashCode() % 1000) + 50.0);

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        int points = 10;
        int intervalMinutes = 60; // 1D

        if ("1H".equalsIgnoreCase(period)) {
            points = 12; // 12 * 5min = 60min
            intervalMinutes = 5;
        } else if ("7D".equalsIgnoreCase(period)) {
            points = 7;
            intervalMinutes = 24 * 60;
        }

        for (int i = 0; i < points; i++) {
            com.smartfin.backend.model.PriceHistory ph = new com.smartfin.backend.model.PriceHistory();
            ph.setSymbol(symbol);
            double variation = (Math.sin(i) * 0.5 + (random.nextDouble() - 0.5)) * (basePrice * 0.02);
            ph.setPrice(BigDecimal.valueOf(basePrice + variation));
            ph.setTimestamp(now.minusMinutes(i * intervalMinutes));
            simulated.add(ph);
        }
        return simulated;
    }

    private BigDecimal simulatePrice(String symbol) {
        // Generate a price that varies slightly around a base implementation or just
        // random
        // In a real app, we might fallback to last known price in DB
        // Here, let's return a "realistic" fake number based on symbol hash to be
        // consistent-ish
        double seed = Math.abs(symbol.hashCode() % 1000) + 50.0;
        double variation = (random.nextDouble() - 0.5) * 10.0; // +/- $5
        return BigDecimal.valueOf(seed + variation);
    }
}
