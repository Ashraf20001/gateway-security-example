package com.supercharge.gateway.config;

import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import com.cbt.supercharge.crypto.core.DataEncryptionUtils;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
@EnableWebFlux
public class MongoConfig implements WebFluxConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);

	@Value("${mongodb.username}")
	private String userName;

	@Value("${mongodb.password}")
	private String password;

	@Value("${mongodb.host}")
	private String host;

	@Value("${mongodb.database}")
	private String database;

	@Value("${mongodb.port}")
	private String port;
	
    @Value("${secure.key}")
    private String secureKey;

	@Bean
	public MongoClient mongoClient() throws Exception {
		logger.info("userName: " + userName);
		logger.info("host: " + host);
		logger.info("port: " + port);
		try {
            String encodedUserName = URLEncoder.encode(userName, "UTF-8");
            String decryptedPassword = DataEncryptionUtils.decryptPwd(password, secureKey);
            String connectionString = String.format("mongodb://%s:%s@%s:%s/%s", encodedUserName, decryptedPassword, host, port, database);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .build();
            MongoClient mongoClient = MongoClients.create(settings);
            mongoClient.listDatabaseNames().first(); // Check DB connection
            return mongoClient;
		} catch (Exception e) {
			logger.error("Invalid UserName and Password", e);
			throw e;
		}
	}

	@Bean
	public SimpleMongoClientDatabaseFactory simpleMongoClientDatabaseFactory() throws Exception {
		return new SimpleMongoClientDatabaseFactory(mongoClient(), database);
	}

	@Bean
	public MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(simpleMongoClientDatabaseFactory());
	}
	
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // Customize this to your required origins
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }

}
