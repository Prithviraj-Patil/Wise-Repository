package edu.ucla.wise.shared.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.c3p0.internal.C3P0ConnectionProvider;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.usertype.UserType;

import edu.ucla.wise.shared.properties.ConnectionPoolProperties;
import edu.ucla.wise.shared.properties.DataBaseProperties;

public class HibernateConfiguration {

    private final SessionFactory factory;

    public HibernateConfiguration(DataBaseProperties properties, List<Class<? extends Object>> annotatedClasses) {
        this(properties, annotatedClasses, new AdditionalOptions());
    }

    public HibernateConfiguration(DataBaseProperties properties, List<Class<? extends Object>> annotatedClasses,
            AdditionalOptions additionalOptions) {

        Properties hibernateProperties = new Properties();
        hibernateProperties.put("hibernate.connection.driver_class", properties.getJdbcDriver());
        hibernateProperties.put("hibernate.connection.url", properties.getJdbcUrl());
        hibernateProperties.put("hibernate.connection.username", properties.getJdbcUsername());
        hibernateProperties.put("hibernate.connection.password", properties.getJdbcPassword());
        hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        hibernateProperties.put("hibernate.show_sql", "true");

        // Store the entire entry that was deleted instead of just the primary
        // key
        hibernateProperties.put("org.hibernate.envers.store_data_at_delete", "true");

        if (additionalOptions.isCachingEnabled()) {
            hibernateProperties.put("hibernate.cache.use_second_level_cache", "true");
            hibernateProperties.put("hibernate.cache.use_query_cache", "true");
            hibernateProperties.put("hibernate.cache.use_structured_entries", "true");
            hibernateProperties.put("hibernate.cache.provider_class", "org.hibernate.cache.EHCacheProvider");
            hibernateProperties.put("hibernate.cache.region.factory_class",
                    "org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory");
        }

        hibernateProperties.putAll(additionalOptions.getProperties());

        Configuration cfg = new Configuration().setProperties(hibernateProperties);

        for (Class<? extends Object> clazz : annotatedClasses) {
            cfg.addAnnotatedClass(clazz);
        }

        for (Entry<UserType, String[]> typeOverrideEntry : additionalOptions.getTypeOverrides().entrySet()) {
            cfg.registerTypeOverride(typeOverrideEntry.getKey(), typeOverrideEntry.getValue());
        }

        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
                .applySettings(cfg.getProperties());
        this.factory = cfg.buildSessionFactory(builder.build());
    }

    public SessionFactory getFactory() {
        return this.factory;
    }

    public void close() {
        /*
         * 
         * C3P0ConnectionProvider not getting closed on SessionFactory.close
         * 
         * https://hibernate.atlassian.net/browse/HHH-7364
         */
        if (this.factory instanceof SessionFactoryImpl) {
            SessionFactoryImpl sf = (SessionFactoryImpl) this.factory;
            ConnectionProvider conn = sf.getConnectionProvider();
            if (conn instanceof C3P0ConnectionProvider) {
                ((C3P0ConnectionProvider) conn).close();
            }
        }

        this.factory.close();
    }

    public static class AdditionalOptions {
        private boolean enableCaching = false;
        private final Properties properties = new Properties();

        private final Map<UserType, String[]> typeOverrides = new HashMap<UserType, String[]>();

        public AdditionalOptions enableCaching() {
            this.enableCaching = true;

            return this;
        }

        public AdditionalOptions addAdditionalProperties(Properties additionalProperties) {
            this.properties.putAll(additionalProperties);

            return this;
        }

        public AdditionalOptions addConnectionPoolProperties(ConnectionPoolProperties connectionProperties) {
            this.properties.put("hibernate.c3p0.min_size", connectionProperties.getMinimumPoolSize());
            this.properties.put("hibernate.c3p0.max_size", connectionProperties.getMaximumPoolSize());
            this.properties.put("hibernate.c3p0.timeout", connectionProperties.getConnectionTimeout());
            this.properties.put("hibernate.c3p0.max_statements", connectionProperties.getPreparedStatementCacheSize());
            this.properties.put("hibernate.c3p0.idle_test_period", connectionProperties.getConnectionIdleTestPeriod());

            return this;
        }

        public boolean isCachingEnabled() {
            return this.enableCaching;
        }

        public Properties getProperties() {
            return this.properties;
        }

        public AdditionalOptions registerTypeOverrides(UserType userType, List<Class<?>> classesList) {

            String[] classes = new String[classesList.size()];

            int index = 0;
            for (Class<? extends Object> clazz : classesList) {
                classes[index] = clazz.getName();
            }

            this.typeOverrides.put(userType, classes);

            return this;
        }

        public Map<UserType, String[]> getTypeOverrides() {
            return this.typeOverrides;
        }
    }

}