package com.project.refvalidator.config

import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource


@Configuration
class DataSourceConfig {

    @Bean("globalDataSource")
    fun globalDataSource(): DataSource {
        return DataSourceBuilder.create()
                .url("jdbc:mariadb://127.0.0.1:3306/ProtonMailGlobal")
                .username("root")
                .password("password")
                .driverClassName("org.mariadb.jdbc.Driver")
                .build()
    }

    @Bean("shardDataSource")
    fun shardDataSource(): DataSource {
        return DataSourceBuilder.create()
                .url("jdbc:mariadb://127.0.0.1:3306/ProtonMailShard")
                .username("root")
                .password("password")
                .driverClassName("org.mariadb.jdbc.Driver")
                .build()
    }

    @Bean("globalTemplate")
    fun globalTemplate(globalDataSource: DataSource): JdbcTemplate {
        return JdbcTemplate(globalDataSource)
    }

    @Bean("shardTemplate")
    fun shardTemplate(shardDataSource: DataSource): JdbcTemplate {
        return JdbcTemplate(shardDataSource)
    }

}