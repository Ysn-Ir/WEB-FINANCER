package com.smartfin.backend.repository;

import com.smartfin.backend.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findBySymbolOrderByTimestampDesc(String symbol);
    // Limit results potentially in service or via Pageable, but basics first
}
