package com.bridgelabz.lmsapi.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Configuration for Datasource
 */
@Configuration
public class DataSourceConfig {
    /**
     * @return Data Source builder
     */
    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(System.getenv().get("driver.classname"));
        dataSourceBuilder.url(System.getenv().get("spring.datasource.url"));
        dataSourceBuilder.username(System.getenv().get("spring.datasource.username"));
        dataSourceBuilder.password(System.getenv().get("spring.datasource.password"));
        return dataSourceBuilder.build();
    }
}
