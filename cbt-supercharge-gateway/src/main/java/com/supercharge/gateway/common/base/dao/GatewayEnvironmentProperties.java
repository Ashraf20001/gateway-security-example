/*

 * @author codeboard
 */
package com.supercharge.gateway.common.base.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.cbt.supercharge.constants.core.ApplicationConstants;
import com.cbt.supercharge.constants.core.PropertyConstants;
import com.cbt.supercharge.utils.core.ApplicationUtils;

/**
 * The Class EnvironmentProperties.
 */
@Configuration
@PropertySource("classpath:/environment.properties")
//@PropertySource(value = "file:///${GATE_WAY_ENVIRONMENT_PROPERTIES}/environment.properties", ignoreResourceNotFound = true)
public class GatewayEnvironmentProperties {

	/**
	 * The data base name.
	 */
	private String dataBaseName = "";

	/**
	 * The environment.
	 */
	@Autowired
	private Environment environment;

	public String getDataSourceUrl() {
		return environment.getProperty(PropertyConstants.MYSQL_DATASOURCE_URL);
	}

	public String getDriverName() {
		return environment.getProperty(PropertyConstants.MYSQL_DRIVER_NAME);
	}

	public Integer getExpiryTime() {
		if (ApplicationUtils.isNotBlank(environment.getProperty(PropertyConstants.RESET_URL_EXPIRATION_LIMIT))) {
			return Integer.parseInt(environment.getProperty(PropertyConstants.RESET_URL_EXPIRATION_LIMIT));
		}
		return ApplicationConstants.DEFAULT_EXPIRATION_TIME;
	}

	/**
	 * Gets the Reset password page URI.
	 *
	 * @return the String value of Reset password page URI
	 */
	public String getForgetPasswordUrl() {
		if (ApplicationUtils.isNotBlank(environment.getProperty(PropertyConstants.CBG_DOMAIN_URL))
				&& ApplicationUtils.isNotBlank(environment.getProperty(PropertyConstants.RESET_PASSWORD))) {
			String cbgDomainUrl = environment.getProperty(PropertyConstants.CBG_DOMAIN_URL);
			cbgDomainUrl = cbgDomainUrl != null ? cbgDomainUrl : "";
			return cbgDomainUrl.concat(environment.getProperty(PropertyConstants.RESET_PASSWORD));
		}
		return null;
	}

	/**
	 * Gets the checks if is kafka enabled.
	 *
	 * @return the checks if is kafka enabled
	 */
	public boolean getIsKafkaEnabled() {
		return false;
	}

	/**
	 * Gets the checks if is versioning enabled.
	 *
	 * @return the checks if is versioning enabled
	 */
	public boolean getIsVersioningEnabled() {
		return false;
	}

	/**
	 * Gets the jdbc driver.
	 *
	 * @return the jdbc driver
	 */
	public String getJdbcDriver() {
		return environment.getProperty(PropertyConstants.MYSQL_DRIVER_NAME);
	}

	/**
	 * Gets the jdbc password.
	 *
	 * @return the jdbc password
	 */
	public String getJdbcPassword() {
		return environment.getProperty(PropertyConstants.MYSQL_PASSWORD);
	}

	/**
	 * Gets the jdbc url.
	 *
	 * @return the jdbc url
	 */
	public String getJdbcUrl() {
		String url = replaceDateBaseName(environment.getProperty(PropertyConstants.MYSQL_DATASOURCE_URL));
		url = url.replace(PropertyConstants.MAIN_APP_MYSQL_IP,
				environment.getProperty(PropertyConstants.MYSQL_IP_ADDRESS));
		url = url.replace(PropertyConstants.MAIN_APP_MYSQL_PORT,
				environment.getProperty(PropertyConstants.MYSQL_PORT_NUMBER));
		return url;
	}

	/**
	 * Gets the jdbc user.
	 *
	 * @return the jdbc user
	 */
	public String getJdbcUser() {
		return environment.getProperty(PropertyConstants.MYSQL_USER_NAME);
	}

	/**
	 * Gets the mongo database.
	 *
	 * @return the mongo database
	 */
	public String getMongoDatabase() {
		return environment.getProperty(PropertyConstants.MONGO_DATABASE);
	}

	/**
	 * Gets the mongo host.
	 *
	 * @return the mongo host
	 */
	public String getMongoHost() {
		return environment.getProperty(PropertyConstants.MONGO_HOST);
	}

	/**
	 * Gets the mongo password.
	 *
	 * @return the mongo password
	 */
	public String getMongoPassword() {
		return environment.getProperty(PropertyConstants.MONGO_PASSWORD);
	}

	/**
	 * Gets the mongo port.
	 *
	 * @return the mongo port
	 */
	public Integer getMongoPort() {
		return Integer.parseInt(environment.getProperty(PropertyConstants.MONGO_PORT));
	}

	/**
	 * Gets the mongo user name.
	 *
	 * @return the mongo user name
	 */
	public String getMongoUserName() {
		return environment.getProperty(PropertyConstants.MONGO_USERNAME);
	}

	/**
	 * Gets the my sql date base.
	 *
	 * @return the my sql date base
	 */
	public String getMySqlDateBase() {
		if (dataBaseName.isEmpty()) {
			dataBaseName = environment.getProperty(PropertyConstants.MYSQL_DATA_BASE);
		}
		return dataBaseName;
	}

	/**
	 * get expire time
	 *
	 * @return expire time
	 */
	public String getOtpExpiryTime() {
		return environment.getProperty(PropertyConstants.OTP_EXPIRY_TIME);
	}

	/**
	 * get Otp length
	 *
	 * @return otp length
	 */
	public Integer getOtpLength() {
		return Integer.parseInt(environment.getProperty(PropertyConstants.OTP_LENGTH));
	}

	public String getSqlIp() {
		return environment.getProperty(PropertyConstants.MYSQL_IP_ADDRESS);
	}

	public String getSqlPort() {
		return environment.getProperty(PropertyConstants.MYSQL_PORT_NUMBER);
	}

	/**
	 * Replace date base name.
	 *
	 * @param data the data
	 * @return the string
	 */
	public String replaceDateBaseName(String data) {
		data = data.replace(PropertyConstants.DB_NAME, getMySqlDateBase());
		return data;
	}

	public String getMysqlMaxIdleTimeout() {
		return environment.getProperty(PropertyConstants.MYSQL_MAX_IDLE_TIMEOUT);
	}

	public String getMysqlMaxIdleTimeExcessConnections() {
		return environment.getProperty(PropertyConstants.MYSQL_MAX_IDLE_TIME_EXCESSCONNECTIONS);
	}

	public Boolean getMaterConnectionOnCheckout() {
		return Boolean.valueOf(environment.getProperty(PropertyConstants.MYSQL_CONNECTION_CHECKOUT));
	}

	public String getMysqlMinPoolSize() {
		return environment.getProperty(PropertyConstants.MYSQL_MIN_POOL_SIZE);
	}

	public String getMysqlMaxPoolSize() {
		return environment.getProperty(PropertyConstants.MYSQL_MAX_POOL_SIZE);
	} 
	public String getMysqlIdleConnectionTestPeriod() {
		return environment.getProperty(PropertyConstants.MYSQL_IDLE_CONNECTION_TEST_PERIOD);
	}
	
	public String getPasskey() {
		return environment.getProperty(PropertyConstants.PASSWORD_KEY);
	}
	
	public String getServedBy() {
		return environment.getProperty(PropertyConstants.SERVED_BY);
	}
	
	public String getJwtHeaderName() {
		return environment.getProperty(PropertyConstants.JWT_HEADER_NAME);
	}
	
}
