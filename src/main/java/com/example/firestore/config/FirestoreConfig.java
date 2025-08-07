package com.example.firestore.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * Configuration class for connecting to Firestore using MongoDB API
 * with Google Cloud Service Account authentication.
 */
public class FirestoreConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(FirestoreConfig.class);
    
    private MongoClient mongoClient;
    private MongoDatabase database;
    private Properties properties;
    
    private static FirestoreConfig instance;
    
    private FirestoreConfig() {
        loadProperties();
        initializeConnection();
    }
    
    /**
     * Singleton pattern to ensure only one connection instance
     */
    public static synchronized FirestoreConfig getInstance() {
        if (instance == null) {
            instance = new FirestoreConfig();
        }
        return instance;
    }
    
    /**
     * Load configuration properties from application.properties
     */
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find application.properties");
            }
            properties.load(input);
            logger.info("Configuration properties loaded successfully");
        } catch (IOException e) {
            logger.error("Error loading properties file", e);
            throw new RuntimeException("Failed to load configuration properties", e);
        }
    }
    
    /**
     * Initialize connection to Firestore using MongoDB API for Compute Engine VMs
     */
    private void initializeConnection() {
        try {
            // Build connection string for Firestore MongoDB API (Compute Engine only)
            String connectionString = buildConnectionString();
            
            // Configure MongoDB client with POJO codec support
            CodecRegistry pojoCodecRegistry = fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build())
            );
            
            MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .codecRegistry(pojoCodecRegistry)
                .applyToConnectionPoolSettings(builder -> {
                    builder.maxSize(Integer.parseInt(properties.getProperty("mongodb.connection.pool.max.size", "100")))
                           .minSize(Integer.parseInt(properties.getProperty("mongodb.connection.pool.min.size", "5")))
                           .maxConnectionIdleTime(30, TimeUnit.SECONDS);
                })
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(Integer.parseInt(properties.getProperty("mongodb.connection.timeout.ms", "30000")), TimeUnit.MILLISECONDS)
                           .readTimeout(Integer.parseInt(properties.getProperty("mongodb.socket.timeout.ms", "30000")), TimeUnit.MILLISECONDS);
                })
                .build();
            
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(properties.getProperty("firestore.database.name", "default-database"));
            
            logger.info("Successfully connected to Firestore using MongoDB API from Compute Engine VM");
            
        } catch (Exception e) {
            logger.error("Failed to initialize Firestore connection", e);
            throw new RuntimeException("Failed to connect to Firestore", e);
        }
    }
    
    /**
     * Build MongoDB connection string for Firestore (Compute Engine VMs only)
     */
    private String buildConnectionString() {
        String projectId = properties.getProperty("google.cloud.project.id");
        String databaseUid = properties.getProperty("firestore.database.uid");
        String location = properties.getProperty("firestore.database.location");
        String databaseName = properties.getProperty("firestore.database.name", "default-database");
        
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalArgumentException("Google Cloud Project ID must be specified in application.properties");
        }
        
        if (databaseUid == null || location == null) {
            throw new IllegalArgumentException("Both firestore.database.uid and firestore.database.location must be specified");
        }
        
        // Use the specific connection format for Compute Engine VMs from Google Cloud docs
        String connectionString = String.format(
            "mongodb://%s.%s.firestore.goog:443/%s?loadBalanced=true&tls=true&retryWrites=false&authMechanism=MONGODB-OIDC&authMechanismProperties=ENVIRONMENT:gcp,TOKEN_RESOURCE:FIRESTORE",
            databaseUid, location, databaseName
        );
        
        logger.debug("Built Compute Engine connection string: mongodb://{}***.firestore.goog:443/{}?loadBalanced=true&tls=true&retryWrites=false&authMechanism=MONGODB-OIDC&authMechanismProperties=ENVIRONMENT:gcp,TOKEN_RESOURCE:FIRESTORE", 
                    databaseUid.substring(0, Math.min(8, databaseUid.length())), databaseName);
        
        return connectionString;
    }
    
    /**
     * Get the MongoDB client instance
     */
    public MongoClient getMongoClient() {
        if (mongoClient == null) {
            throw new IllegalStateException("MongoDB client not initialized");
        }
        return mongoClient;
    }
    
    /**
     * Get the MongoDB database instance
     */
    public MongoDatabase getDatabase() {
        if (database == null) {
            throw new IllegalStateException("Database not initialized");
        }
        return database;
    }
    
    /**
     * Get configuration property value
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Get configuration property value with default
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Test the connection to Firestore
     */
    public boolean testConnection() {
        try {
            // Simple ping to test connectivity
            database.runCommand(new org.bson.Document("ping", 1));
            logger.info("Connection test successful");
            return true;
        } catch (Exception e) {
            logger.error("Connection test failed", e);
            return false;
        }
    }
    
    /**
     * Close the MongoDB client and release resources
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            logger.info("MongoDB client connection closed");
        }
    }
    
    /**
     * Cleanup method to ensure proper resource disposal
     */
    public static void shutdown() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }
}
