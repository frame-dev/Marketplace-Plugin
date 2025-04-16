package ch.framedev.marketplace.database;

import ch.framedev.marketplace.utils.ConfigUtils;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoDBClient {

    private static final Logger LOGGER = Logger.getLogger(MongoDBClient.class.getName());

    private MongoClient client;
    private MongoDatabase mongoDatabase;

    /**
     * Constructor for MongoDBClient.
     * Initializes the MongoDB client and connects to the database.
     */
    public MongoDBClient() {
        if (ConfigUtils.MONGODB_USE_URI) {
            connectWithUri();
        } else {
            connectWithCredentials();
        }
        testConnection();
    }

    private void connectWithUri() {
        try {
            if (ConfigUtils.MONGODB_URI != null) {
                client = MongoClients.create(ConfigUtils.MONGODB_URI);
                if (ConfigUtils.MONGODB_DATABASE != null) {
                    this.mongoDatabase = client.getDatabase(ConfigUtils.MONGODB_DATABASE);
                } else {
                    LOGGER.warning("MongoDB database name is null. Please check your configuration.");
                }
            } else {
                LOGGER.warning("MongoDB URI is null. Please check your configuration.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to MongoDB using URI.", e);
        }
    }

    private void connectWithCredentials() {
        try {
            if (ConfigUtils.MONGODB_USERNAME != null && ConfigUtils.MONGODB_PASSWORD != null && ConfigUtils.MONGODB_DATABASE != null) {
                MongoCredential credential = MongoCredential.createCredential(
                        ConfigUtils.MONGODB_USERNAME,
                        ConfigUtils.MONGODB_DATABASE,
                        ConfigUtils.MONGODB_PASSWORD.toCharArray()
                );

                this.client = MongoClients.create(
                        MongoClientSettings.builder()
                                .credential(credential)
                                .applyToClusterSettings(builder ->
                                        builder.hosts(Collections.singletonList(new ServerAddress(ConfigUtils.MONGODB_HOST, ConfigUtils.MONGODB_PORT))))
                                .build()
                );
                this.mongoDatabase = client.getDatabase(ConfigUtils.MONGODB_DATABASE);
            } else {
                LOGGER.warning("MongoDB credentials or database name is null. Please check your configuration.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to MongoDB using credentials.", e);
        }
    }

    private void testConnection() {
        if (client != null) {
            try {
                mongoDatabase.listCollections(); // Test query
                LOGGER.info("Successfully connected to MongoDB.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "MongoDB connection test failed.", e);
            }
        } else {
            LOGGER.severe("MongoDB client is null. Connection was not established.");
        }
    }

    public void close() {
        if (client != null) {
            client.close();
            LOGGER.info("MongoDB connection closed.");
        }
    }

    public MongoClient getClient() {
        return client;
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }
}