package com.example.batch_demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pos_sale")
public class PosSale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id")
    private Long transactionId;
    
    private LocalDate date;
    
    @Column(name = "store_id")
    private String storeId;
    
    @Column(name = "product_id")
    private String productId;
    
    private Integer quantity;
    
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    
    private Integer state;
    
    private String unikey;
    
    @Column(name = "create_date")
    private LocalDateTime createDate;
    
    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
        unikey = date.toString() + "_" + storeId + "_" + productId;
    }
} 