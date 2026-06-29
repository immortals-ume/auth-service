package com.immortals.auth.service;

import com.immortals.auth.service.config.CookieProperties;
import com.immortals.auth.service.config.CorsProperties;
import com.immortals.auth.service.config.DatabaseProperties;
import com.immortals.auth.service.config.JwtKeyProperties;
import com.immortals.auth.service.config.ReadDataSourceProperties;
import com.immortals.auth.service.config.ServiceClientProperties;
import com.immortals.auth.service.config.WriteDataSourceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
@EnableAsync
@EnableScheduling
@EnableDiscoveryClient
@EnableConfigurationProperties({
        CookieProperties.class,
        CorsProperties.class,
        DatabaseProperties.class,
        JwtKeyProperties.class,
        ReadDataSourceProperties.class,
        ServiceClientProperties.class,
        WriteDataSourceProperties.class
})
public class  AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

