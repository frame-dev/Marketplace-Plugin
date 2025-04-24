package ch.framedev.marketplace.database;

import ch.framedev.marketplace.utils.ConfigVariables;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class MongoDBClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBClient.class.getName());

    private MongoClient client;
    private MongoDatabase mongoDatabase;

    /**
     * Constructor for MongoDBClient.
     * Initializes the MongoDB client and connects to the database.
     */
    public MongoDBClient() {
        if (ConfigVariables.MONGODB_USE_URI) {
            connectWithUri();
        } else {
            connectWithCredentials();
        }
        testConnection();
    }

    /**
     * Connect to MongoDB with Uri
     */
    private void connectWithUri() {
        try {
            if (ConfigVariables.MONGODB_URI != null) {
                // Create the MongoDB Client
                client = MongoClients.create(ConfigVariables.MONGODB_URI);
                if (ConfigVariables.MONGODB_DATABASE != null) {
                    // Setup Database
                    this.mongoDatabase = client.getDatabase(ConfigVariables.MONGODB_DATABASE);
                } else {
                    LOGGER.warn("MongoDB database name is null. Please check your configuration.");
                }
            } else {
                LOGGER.warn("MongoDB URI is null. Please check your configuration.");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to connect to MongoDB using URI.", e);
        }
    }

    /**
     * Connect MongoDB with Credentials
     */
    private void connectWithCredentials() {
        try {
            if (ConfigVariables.MONGODB_USERNAME != null && ConfigVariables.MONGODB_PASSWORD != null && ConfigVariables.MONGODB_DATABASE != null) {
                MongoCredential credential = MongoCredential.createCredential(
                        ConfigVariables.MONGODB_USERNAME,
                        ConfigVariables.MONGODB_DATABASE,
                        ConfigVariables.MONGODB_PASSWORD.toCharArray()
                );

                // Create the MongoDB Client
                this.client = MongoClients.create(
                        MongoClientSettings.builder()
                                .credential(credential)
                                .applyToClusterSettings(builder ->
                                        builder.hosts(Collections.singletonList(new ServerAddress(ConfigVariables.MONGODB_HOST, ConfigVariables.MONGODB_PORT))))
                                .build()
                );
                // Setup Database
                this.mongoDatabase = client.getDatabase(ConfigVariables.MONGODB_DATABASE);
            } else {
                LOGGER.warn("MongoDB credentials or database name is null. Please check your configuration.");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to connect to MongoDB using credentials.", e);
        }
    }

    private void testConnection() {
        if (client != null) {
            try {
                mongoDatabase.listCollections(); // Test query
                LOGGER.info("Successfully connected to MongoDB.");
            } catch (Exception e) {
                LOGGER.error("MongoDB connection test failed.", e);
            }
        } else {
            LOGGER.error("MongoDB client is null. Connection was not established.");
        }
    }

    /**
     * This Method returns the MongoDB Database
     * @return returns the Mongo Database
     */
    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }
}