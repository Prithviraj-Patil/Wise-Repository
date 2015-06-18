package edu.ucla.wise.shared.properties;

public interface ConnectionPoolProperties {

	Object getPreparedStatementCacheSize();

	Object getMinimumPoolSize();

	Object getMaximumPoolSize();

	Object getConnectionTimeout();

	Object getConnectionIdleTestPeriod();

}
