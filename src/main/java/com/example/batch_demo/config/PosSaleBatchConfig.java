package com.example.batch_demo.config;

import com.example.batch_demo.entity.PosSale;
import com.example.batch_demo.entity.PosSaleSummary;
import com.example.batch_demo.repository.PosSaleRepository;
import com.example.batch_demo.repository.PosSaleSummaryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class PosSaleBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PosSaleRepository posSaleRepository;
    private final PosSaleSummaryRepository posSaleSummaryRepository;

    @Bean
    public FlatFileItemReader<PosSale> reader() {
        return new FlatFileItemReaderBuilder<PosSale>()
                .name("posSaleReader")
                .resource(new FileSystemResource("file/pos_sale.csv"))
                .delimited()
                .names("transactionId", "date", "storeId", "productId", "quantity", "unitPrice", "totalAmount")
                .linesToSkip(1)
                .fieldSetMapper(fieldSet -> {
                    PosSale posSale = new PosSale();
                    posSale.setTransactionId(Long.parseLong(fieldSet.readString("transactionId")));
                    posSale.setDate(LocalDate.parse(fieldSet.readString("date")));
                    posSale.setStoreId(fieldSet.readString("storeId"));
                    posSale.setProductId(fieldSet.readString("productId"));
                    posSale.setQuantity(fieldSet.readInt("quantity"));
                    posSale.setUnitPrice(new BigDecimal(fieldSet.readString("unitPrice")));
                    posSale.setTotalAmount(new BigDecimal(fieldSet.readString("totalAmount")));
                    posSale.setState(1);
                    return posSale;
                })
                .build();
    }

    @Bean
    public Step importCsvStep() {
        return new StepBuilder("importCsvStep", jobRepository)
                .<PosSale, PosSale>chunk(100, transactionManager)
                .reader(reader())
                .writer(chunk -> posSaleRepository.saveAll(chunk.getItems()))
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setConcurrencyLimit(5);
        return executor;
    }

    @Bean
    public Step processingStep() {
        return new StepBuilder("processingStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    List<String> storeIds = posSaleRepository.findDistinctStoreIds();
                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    
                    for (String storeId : storeIds) {
                        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                            processStore(storeId);
                        }, taskExecutor());
                        futures.add(future);
                    }
                    
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Transactional
    protected void processStore(String storeId) {
        List<PosSale> sales = posSaleRepository.findByStateAndStoreId(1, storeId);
        
        Map<String, Integer> summaries = sales.stream()
                .collect(Collectors.groupingBy(PosSale::getUnikey,
                        Collectors.summingInt(PosSale::getQuantity)));
        
        summaries.forEach((unikey, totalQuantity) -> {
            PosSale sample = sales.stream()
                    .filter(s -> s.getUnikey().equals(unikey))
                    .findFirst()
                    .orElseThrow();
            
            PosSaleSummary summary = new PosSaleSummary();
            summary.setDate(sample.getDate());
            summary.setStoreCode(sample.getStoreId());
            summary.setProduct(sample.getProductId());
            summary.setQuantity(totalQuantity);
            
            posSaleSummaryRepository.save(summary);
            
            sales.stream()
                    .filter(s -> s.getUnikey().equals(unikey))
                    .forEach(s -> {
                        s.setState(2);
                        posSaleRepository.save(s);
                    });
        });
    }

    @Bean
    public Job posSaleJob() {
        return new JobBuilder("posSaleJob", jobRepository)
                .start(importCsvStep())
                .next(processingStep())
                .build();
    }
} 