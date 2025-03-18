package com.example.batch_demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "job_config")
public class JobConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "job_name", nullable = false)
    private String jobName;
    
    @Column(nullable = false)
    private boolean enabled = true;
    
    @Column(name = "cron_expression", nullable = false)
    private String cronExpression;
    
    @Column(name = "last_run")
    private LocalDateTime lastRun;
    
    @Column(name = "next_run")
    private LocalDateTime nextRun;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 