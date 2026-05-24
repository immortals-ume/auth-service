package com.immortals.auth.service.db.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Database configuration properties
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "datasource.core")
public class DatabaseProperties {

    private boolean enabled;
    private String defaultSchema;
    private int connectionTimeout;
    private int maxPoolSize;
    private boolean showSql;
    private String ddlAuto;

    private boolean generateDdl;

    private HibernateProperties hibernate = new HibernateProperties();

    @Getter
    @Setter
    public static class HibernateProperties {
        private String ddlAuto;
        private String dialect;
        private boolean formatSql;

        private Envers envers = new Envers();

        @Getter
        @Setter
        public static class Envers {
            private String auditTableSuffix;
            private String revisionFieldName;
            private String revisionTypeFieldName;
            private boolean storeDataAtDelete;
            private String defaultSchema;
        }
    }
}