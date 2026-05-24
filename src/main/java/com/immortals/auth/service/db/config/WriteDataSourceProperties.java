package com.immortals.auth.service.db.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "datasource.write")
public class WriteDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
