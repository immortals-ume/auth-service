package com.immortals.auth.service.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfig {

    @Bean
    @DependsOn("writeDataSource")
    public SpringLiquibase liquibase(
            @Qualifier("writeDataSource") DataSource writeDataSource,
            @Value("${spring.liquibase.change-log:classpath:db/changelog-master.xml}") String changeLog,
            @Value("${spring.liquibase.enabled:true}") boolean enabled,
            @Value("${spring.liquibase.liquibase-schema:public}") String liquibaseSchema) {

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(writeDataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setShouldRun(enabled);
        liquibase.setLiquibaseSchema(liquibaseSchema);
        liquibase.setDropFirst(Boolean.FALSE);
        liquibase.setClearCheckSums(Boolean.FALSE);
        liquibase.setTestRollbackOnUpdate(Boolean.FALSE);

        return liquibase;
    }
}

