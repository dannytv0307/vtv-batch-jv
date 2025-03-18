package com.example.batch_demo.repository;

import com.example.batch_demo.entity.PosSaleSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosSaleSummaryRepository extends JpaRepository<PosSaleSummary, Long> {
} 