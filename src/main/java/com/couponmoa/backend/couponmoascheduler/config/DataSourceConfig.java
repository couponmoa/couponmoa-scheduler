//package com.couponmoa.backend.couponmoascheduler.config;
//
//import org.springframework.boot.autoconfigure.batch.BatchDataSource;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
//import javax.sql.DataSource;
//
//@Configuration
//public class DataSourceConfig {
//
//    @Bean
//    @Primary
//    @ConfigurationProperties(prefix = "spring.domain-datasource")
//    public DataSource domainDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean
//    @BatchDataSource
//    @ConfigurationProperties(prefix = "spring.batch-datasource")
//    public DataSource batchDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//}
