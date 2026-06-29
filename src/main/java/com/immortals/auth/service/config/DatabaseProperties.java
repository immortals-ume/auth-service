package com.immortals.auth.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

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

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getDefaultSchema() { return defaultSchema; }
    public void setDefaultSchema(String defaultSchema) { this.defaultSchema = defaultSchema; }

    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }

    public int getMaxPoolSize() { return maxPoolSize; }
    public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }

    public boolean isShowSql() { return showSql; }
    public void setShowSql(boolean showSql) { this.showSql = showSql; }

    public String getDdlAuto() { return ddlAuto; }
    public void setDdlAuto(String ddlAuto) { this.ddlAuto = ddlAuto; }

    public boolean isGenerateDdl() { return generateDdl; }
    public void setGenerateDdl(boolean generateDdl) { this.generateDdl = generateDdl; }

    public HibernateProperties getHibernate() { return hibernate; }
    public void setHibernate(HibernateProperties hibernate) { this.hibernate = hibernate; }

    public static class HibernateProperties {
        private String ddlAuto;
        private String dialect;
        private boolean formatSql;
        private Envers envers = new Envers();

        public String getDdlAuto() { return ddlAuto; }
        public void setDdlAuto(String ddlAuto) { this.ddlAuto = ddlAuto; }

        public String getDialect() { return dialect; }
        public void setDialect(String dialect) { this.dialect = dialect; }

        public boolean isFormatSql() { return formatSql; }
        public void setFormatSql(boolean formatSql) { this.formatSql = formatSql; }

        public Envers getEnvers() { return envers; }
        public void setEnvers(Envers envers) { this.envers = envers; }

        public static class Envers {
            private String auditTableSuffix;
            private String revisionFieldName;
            private String revisionTypeFieldName;
            private boolean storeDataAtDelete;
            private String defaultSchema;
            private boolean autoRegisterListeners = true;

            public String getAuditTableSuffix() { return auditTableSuffix; }
            public void setAuditTableSuffix(String auditTableSuffix) { this.auditTableSuffix = auditTableSuffix; }

            public String getRevisionFieldName() { return revisionFieldName; }
            public void setRevisionFieldName(String revisionFieldName) { this.revisionFieldName = revisionFieldName; }

            public String getRevisionTypeFieldName() { return revisionTypeFieldName; }
            public void setRevisionTypeFieldName(String revisionTypeFieldName) { this.revisionTypeFieldName = revisionTypeFieldName; }

            public boolean isStoreDataAtDelete() { return storeDataAtDelete; }
            public void setStoreDataAtDelete(boolean storeDataAtDelete) { this.storeDataAtDelete = storeDataAtDelete; }

            public String getDefaultSchema() { return defaultSchema; }
            public void setDefaultSchema(String defaultSchema) { this.defaultSchema = defaultSchema; }

            public boolean isAutoRegisterListeners() { return autoRegisterListeners; }
            public void setAutoRegisterListeners(boolean autoRegisterListeners) { this.autoRegisterListeners = autoRegisterListeners; }
        }
    }
}

