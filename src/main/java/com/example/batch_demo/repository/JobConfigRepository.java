package com.example.batch_demo.repository;

import com.example.batch_demo.entity.JobConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobConfigRepository extends JpaRepository<JobConfig, Long> {
    Optional<JobConfig> findByJobNameAndEnabled(String jobName, boolean enabled);
} 