package com.example.batch_demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pos_sale_summary")
public class PosSaleSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDate date;
    
    @Column(name = "store_code")
    private String storeCode;
    
    private String product;
    
    private Integer quantity;
    
    @Column(name = "create_date")
    private LocalDateTime createDate;
    
    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
    }
} 