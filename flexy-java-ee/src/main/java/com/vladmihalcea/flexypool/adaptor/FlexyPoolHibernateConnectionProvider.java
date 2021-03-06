package com.vladmihalcea.flexypool.adaptor;

import com.vladmihalcea.flexypool.FlexyPoolDataSource;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * <code>FlexyPoolHibernateConnectionProvider</code> - This is the Hibernate custom {@link DataSource} adapter.
 * It allows registering the {@link com.vladmihalcea.flexypool.FlexyPoolDataSource} instead of the original
 * {@link javax.sql.DataSource} defined in the persistence.xml descriptor.
 *
 * @author Vlad Mihalcea
 * @since 1.2
 */
public class FlexyPoolHibernateConnectionProvider extends DatasourceConnectionProviderImpl {

    private static final long serialVersionUID = -1828184937430573196L;

    private static final Logger LOGGER = LoggerFactory.getLogger(FlexyPoolHibernateConnectionProvider.class);

    private transient FlexyPoolDataSource<DataSource> flexyPoolDataSource;

    public static final String FLEXY_POOL_PROPERTY_PREFIX = "flexy.pool";

    /**
     * Substitute the already configured {@link DataSource} with the {@link FlexyPoolDataSource}
     *
     * @param props JPA/Hibernate properties
     */
    @Override
    public void configure(Map props) {
        super.configure(props);
        LOGGER.debug("Hibernate switched to using FlexyPoolDataSource");
        Properties overridingProperties = new Properties();
        for(Map.Entry<String, Object> propsEntry : ((Map<String, Object>) props).entrySet()) {
            if(propsEntry.getKey().startsWith( FLEXY_POOL_PROPERTY_PREFIX ) &&
                propsEntry.getValue() instanceof String) {
                overridingProperties.setProperty(
                    propsEntry.getKey(),
                    (String) propsEntry.getValue()
                );
            }
        }
        flexyPoolDataSource = new FlexyPoolDataSource<DataSource>(getDataSource(), overridingProperties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() throws SQLException {
        return flexyPoolDataSource.getConnection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes")
    public boolean isUnwrappableAs(Class unwrapType) {
        return super.isUnwrappableAs(unwrapType) || FlexyPoolHibernateConnectionProvider.class.isAssignableFrom(unwrapType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        flexyPoolDataSource.stop();
        super.stop();
    }
}
