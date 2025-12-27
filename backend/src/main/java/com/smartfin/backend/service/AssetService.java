package com.smartfin.backend.service;

import com.smartfin.backend.model.Asset;
import com.smartfin.backend.model.User;
import com.smartfin.backend.repository.AssetRepository;
import com.smartfin.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private com.smartfin.backend.repository.PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Asset> getAssetsForUser(String username) {
        List<Asset> assets = assetRepository.findByUserUsername(username);
        assets.forEach(asset -> updateAssetPriceIfNeeded(asset, false));
        return assets;
    }

    public Asset createAsset(Asset asset, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        asset.setUser(user);

        // Initial price fetch
        updateAssetPriceIfNeeded(asset, true);

        return assetRepository.save(asset);
    }

    // Force refresh endpoint calls this
    public void updateAssetPrices(String username) {
        List<Asset> assets = assetRepository.findByUserUsername(username);
        for (Asset asset : assets) {
            updateAssetPriceIfNeeded(asset, true);
        }
    }

    private void updateAssetPriceIfNeeded(Asset asset, boolean force) {
        // Update if forced, price is missing, or price is older than 5 minutes
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().minusMinutes(5);

        boolean isStale = asset.getCurrentPrice() == null ||
                asset.getLastUpdate() == null ||
                asset.getLastUpdate().isBefore(cutoff);

        if (force || isStale) {
            try {
                BigDecimal currentPrice = marketDataService.getPrice(asset.getSymbol(), asset.getType());
                asset.setCurrentPrice(currentPrice);
                asset.setLastUpdate(java.time.LocalDateTime.now());
                assetRepository.save(asset);

                // Save History
                com.smartfin.backend.model.PriceHistory history = new com.smartfin.backend.model.PriceHistory();
                history.setSymbol(asset.getSymbol());
                history.setPrice(currentPrice);
                history.setTimestamp(java.time.LocalDateTime.now());
                priceHistoryRepository.save(history);

                // Minimal delay to be nice to APIs if we are looping, but not blocking user too
                // much
                // relying on MarketDataService simulation if we fail
            } catch (Exception e) {
                System.err.println("Failed to update asset " + asset.getSymbol() + ": " + e.getMessage());
                // On failure, we do nothing, so the asset retains its existing 'currentPrice'
                // from the DB.
            }
        }
    }

    public BigDecimal calculatePortfolioValue(String username) {
        List<Asset> assets = getAssetsForUser(username);
        BigDecimal total = BigDecimal.ZERO;
        for (Asset asset : assets) {
            BigDecimal price = asset.getCurrentPrice();
            if (price == null) {
                // Fallback if never updated: use cost or fetch now
                price = asset.getAvgCost(); // Simplest fallback
            }
            total = total.add(price.multiply(asset.getQuantity()));
        }
        return total;
    }

    public Asset updateAsset(Long id, Asset assetUpdates, String username) {
        Asset existingAsset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        if (!existingAsset.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        existingAsset.setSymbol(assetUpdates.getSymbol());
        existingAsset.setName(assetUpdates.getName());
        existingAsset.setQuantity(assetUpdates.getQuantity());
        existingAsset.setAvgCost(assetUpdates.getAvgCost());
        existingAsset.setType(assetUpdates.getType());

        return assetRepository.save(existingAsset);
    }

    public void deleteAsset(Long id, String username) {
        Asset existingAsset = assetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        if (!existingAsset.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        assetRepository.delete(existingAsset);
    }
}
