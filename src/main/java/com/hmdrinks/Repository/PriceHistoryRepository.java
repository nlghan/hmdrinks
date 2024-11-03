package com.hmdrinks.Repository;

import com.hmdrinks.Entity.PriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Integer> {
    Page<PriceHistory> findByProductVariant_VarId(int varId, Pageable pageable);
    Page<PriceHistory> findAll(Pageable pageable);
}
