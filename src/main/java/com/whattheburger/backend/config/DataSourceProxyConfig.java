package com.whattheburger.backend.config;

import com.zaxxer.hikari.HikariDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("sql-debug")
public class DataSourceProxyConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource realDataSource(
            DataSourceProperties properties) {

        return properties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Primary
    public DataSource dataSource(
        @Qualifier("realDataSource") DataSource realDataSource) {
        return ProxyDataSourceBuilder
                .create(realDataSource)
                .name("SQL-DEBUG")
                .countQuery()
                .build();
    }
}
