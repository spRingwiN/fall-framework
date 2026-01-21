package com.eric.fall.jdbc.without.tx;

import com.eric.fall.annotation.*;
import com.eric.fall.jdbc.JdbcTemplate;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

@ComponentScan
@Configuration
public class JdbcWithoutTxApplication {

    @Bean(destroyMethod = "close")
    DataSource dataSource(
            // properties:
            @Value("${fall.datasource.url}") String url, //
            @Value("${fall.datasource.username}") String username, //
            @Value("${fall.datasource.password}") String password, //
            @Value("${fall.datasource.driver-class-name:}") String driver, //
            @Value("${fall.datasource.maximum-pool-size:20}") int maximumPoolSize, //
            @Value("${fall.datasource.minimum-pool-size:1}") int minimumPoolSize, //
            @Value("${fall.datasource.connection-timeout:30000}") int connTimeout //
    ) {
        var config = new HikariConfig();
        config.setAutoCommit(false);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        if (driver != null) {
            config.setDriverClassName(driver);
        }
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumPoolSize);
        config.setConnectionTimeout(connTimeout);
        return new HikariDataSource(config);
    }

    @Bean
    JdbcTemplate jdbcTemplate(@Autowired DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }



}
