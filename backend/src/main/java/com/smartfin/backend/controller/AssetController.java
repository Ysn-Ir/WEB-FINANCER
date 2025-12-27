package com.smartfin.backend.controller;

import com.smartfin.backend.model.Asset;
import com.smartfin.backend.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "http://localhost:4200")
public class AssetController {

    @Autowired
    private com.smartfin.backend.service.AssetService assetService;

    @GetMapping
    public List<Asset> getAllAssets(java.security.Principal principal) {
        return assetService.getAssetsForUser(principal.getName());
    }

    @PostMapping
    public Asset createAsset(@RequestBody Asset asset, java.security.Principal principal) {
        // Use the service to handle user association
        return assetService.createAsset(asset, principal.getName());
    }

    @PutMapping("/{id}")
    public Asset updateAsset(@PathVariable Long id, @RequestBody Asset asset, java.security.Principal principal) {
        return assetService.updateAsset(id, asset, principal.getName());
    }

    @DeleteMapping("/{id}")
    public void deleteAsset(@PathVariable Long id, java.security.Principal principal) {
        assetService.deleteAsset(id, principal.getName());
    }

    @PostMapping("/refresh")
    public void refreshPrices(java.security.Principal principal) {
        assetService.updateAssetPrices(principal.getName());
    }
}
