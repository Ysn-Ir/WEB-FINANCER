package com.smartfin.backend.service;

import com.smartfin.backend.model.Asset;
import com.smartfin.backend.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class ForecastingService {

    @Autowired
    private AssetRepository assetRepository;

    public Map<String, Object> generateForecast(String username, int years, double annualReturn, Double monthlyContribution) {
        List<Asset> assets = assetRepository.findByUserUsername(username);
        
        // Calculate initial portfolio value
        BigDecimal initialValue = BigDecimal.ZERO;
        for (Asset asset : assets) {
            BigDecimal price = asset.getCurrentPrice() != null ? asset.getCurrentPrice() : asset.getAvgCost();
            if (price != null) {
                initialValue = initialValue.add(price.multiply(asset.getQuantity()));
            }
        }

        double startValue = initialValue.doubleValue();
        double contribution = monthlyContribution != null ? monthlyContribution : 0.0;
        int months = years * 12;
        
        // Simulation parameters
        int simulations = 500; // Monte Carlo iterations
        
        // Geometric Brownian Motion Parameters
        double dt = 1.0 / 12.0; // Time step (1 month)
        double mu = annualReturn / 100.0; // Annual expected return (Drift)
        double sigma = 0.15; // Annual Volatility (Standard Deviation)
        
        // GBM Drift and Diffusion components
        double driftComponent = (mu - (sigma * sigma) / 2.0) * dt;
        double volatilityComponent = sigma * Math.sqrt(dt);
        
        Random random = new Random();
        
        // Store all paths: paths[month][simulationIndex]
        double[][] paths = new double[months + 1][simulations];
        
        // Initialize month 0
        for (int s = 0; s < simulations; s++) {
            paths[0][s] = startValue;
        }
        
        // Run simulations
        for (int i = 1; i <= months; i++) {
            for (int s = 0; s < simulations; s++) {
                double prevValue = paths[i-1][s];
                
                // S_t = S_{t-1} * exp( (mu - sigma^2/2)dt + sigma * epsilon * sqrt(dt) )
                double shock = random.nextGaussian();
                double multiplier = Math.exp(driftComponent + volatilityComponent * shock);
                
                // Value depends on previous value growing, plus new contribution
                double newValue = (prevValue + contribution) * multiplier;
                
                paths[i][s] = Math.max(0, newValue);
            }
        }
        
        // Extract Percentiles
        List<Map<String, Object>> expectedScenario = new ArrayList<>();
        List<Map<String, Object>> optimisticScenario = new ArrayList<>();
        List<Map<String, Object>> conservativeScenario = new ArrayList<>();
        
        for (int i = 0; i <= months; i++) {
            List<Double> monthValues = new ArrayList<>();
            for (int s = 0; s < simulations; s++) {
                monthValues.add(paths[i][s]);
            }
            
            // Critical: Sort to find percentiles
            Collections.sort(monthValues);
            
            // 90th percentile = Optimistic
            double optVal = monthValues.get((int)(simulations * 0.9));
            // 50th percentile = Expected
            double expVal = monthValues.get((int)(simulations * 0.5));
            // 10th percentile = Conservative
            double consVal = monthValues.get((int)(simulations * 0.1));
            
            Map<String, Object> pExp = new HashMap<>(); pExp.put("month", i); pExp.put("totalValue", expVal); expectedScenario.add(pExp);
            Map<String, Object> pOpt = new HashMap<>(); pOpt.put("month", i); pOpt.put("totalValue", optVal); optimisticScenario.add(pOpt);
            Map<String, Object> pCons = new HashMap<>(); pCons.put("month", i); pCons.put("totalValue", consVal); conservativeScenario.add(pCons);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("expectedScenario", expectedScenario);
        result.put("optimisticScenario", optimisticScenario);
        result.put("conservativeScenario", conservativeScenario);
        result.put("initialValue", startValue);
        return result;
    }
}
