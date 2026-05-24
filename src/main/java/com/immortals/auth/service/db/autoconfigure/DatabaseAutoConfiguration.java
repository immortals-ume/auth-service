package com.immortals.auth.service.db.autoconfigure;

import com.immortals.auth.service.db.config.DatabaseProperties;
import com.immortals.auth.service.db.props.DbType;
import com.immortals.auth.service.db.config.ReadDataSourceProperties;
import com.immortals.auth.service.db.config.WriteDataSourceProperties;
import com.immortals.auth.service.db.routing.RoutingDataSource;
import com.immortals.auth.service.repository.AuthRepository;
import com.immortals.auth.service.repository.RoleRepository;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnClass(DataSource.class)
@EnableTransactionManagement
@EnableConfigurationProperties({
        ReadDataSourceProperties.class,
        WriteDataSourceProperties.class,
        DatabaseProperties.class
})
@EnableJpaRepositories(
        basePackageClasses = {AuthRepository.class, RoleRepository.class}, entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
@EntityScan("com.immortals.auth.service.domain")
@RequiredArgsConstructor
public  class DatabaseAutoConfiguration {

    private final WriteDataSourceProperties writeProps;
    private final ReadDataSourceProperties readProps;
    private final DatabaseProperties dbProps;

    @Bean(name = "writeDataSource")
    @ConditionalOnMissingBean(name = "writeDataSource")
    public DataSource writeDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(writeProps.getUrl())
                .username(writeProps.getUsername())
                .password(writeProps.getPassword())
                .driverClassName(writeProps.getDriverClassName())
                .build();
    }


    @Bean(name = "readDataSource")
    @ConditionalOnMissingBean(name = "readDataSource")
    public DataSource readDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(readProps.getUrl())
                .username(readProps.getUsername())
                .password(readProps.getPassword())
                .driverClassName(readProps.getDriverClassName())
                .build();
    }


    @Bean(name = "routingDataSource")
    @ConditionalOnMissingBean(name = "routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("writeDataSource") DataSource writeDataSource,
            @Qualifier("readDataSource") DataSource readDataSource) {

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DbType.WRITE, writeDataSource);
        targetDataSources.put(DbType.READ, readDataSource);

        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(writeDataSource);
        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }

    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return routingDataSource;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(dbProps.isShowSql());
        adapter.setDatabasePlatform(dbProps.getHibernate().getDialect());
        return adapter;
    }

    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(
            JpaVendorAdapter adapter) {

        return new EntityManagerFactoryBuilder(
                adapter,
                dataSource -> buildJpaProperties(),
                null
        );
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("dataSource") DataSource dataSource) {

        return builder
                .dataSource(dataSource)
                .packages("com.immortals.auth.service.domain.entity")
                .persistenceUnit("authPU")
                .properties(buildJpaProperties())
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(
            EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
    private Map<String, Object> buildJpaProperties() {
        Map<String, Object> props = new HashMap<>();

        props.put("hibernate.hbm2ddl.auto", dbProps.getHibernate().getDdlAuto());
        props.put("hibernate.dialect", dbProps.getHibernate().getDialect());
        props.put("hibernate.format_sql", dbProps.getHibernate().isFormatSql());
        props.put("hibernate.show_sql", dbProps.isShowSql());

        DatabaseProperties.HibernateProperties.Envers envers = dbProps.getHibernate().getEnvers();

        props.put("org.hibernate.envers.audit_table_suffix", envers.getAuditTableSuffix());
        props.put("org.hibernate.envers.revision_field_name", envers.getRevisionFieldName());
        props.put("org.hibernate.envers.revision_type_field_name", envers.getRevisionTypeFieldName());
        props.put("org.hibernate.envers.store_data_at_delete", envers.isStoreDataAtDelete());
        props.put("org.hibernate.envers.default_schema", envers.getDefaultSchema());

        return props;
    }
}