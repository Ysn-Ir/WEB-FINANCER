package com.smartfin.backend.repository;

import com.smartfin.backend.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByUserId(Long userId);

    List<Asset> findByUserUsername(String username);
}
