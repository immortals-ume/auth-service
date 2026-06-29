package com.immortals.auth.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datasource.write")
public class WriteDataSourceProperties {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private String poolName = "write-pool";
    private int maximumPoolSize = 10;
    private int minimumIdle = 5;
    private long connectionTimeout = 30_000;
    private long idleTimeout = 600_000;
    private long maxLifetime = 1_800_000;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDriverClassName() { return driverClassName; }
    public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }

    public String getPoolName() { return poolName; }
    public void setPoolName(String poolName) { this.poolName = poolName; }

    public int getMaximumPoolSize() { return maximumPoolSize; }
    public void setMaximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; }

    public int getMinimumIdle() { return minimumIdle; }
    public void setMinimumIdle(int minimumIdle) { this.minimumIdle = minimumIdle; }

    public long getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }

    public long getIdleTimeout() { return idleTimeout; }
    public void setIdleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; }

    public long getMaxLifetime() { return maxLifetime; }
    public void setMaxLifetime(long maxLifetime) { this.maxLifetime = maxLifetime; }
}

