package com.example.batch_demo.repository;

import com.example.batch_demo.entity.PosSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PosSaleRepository extends JpaRepository<PosSale, Long> {
    List<PosSale> findByStateAndStoreId(Integer state, String storeId);
    
    @Query("SELECT DISTINCT p.storeId FROM PosSale p")
    List<String> findDistinctStoreIds();
} 