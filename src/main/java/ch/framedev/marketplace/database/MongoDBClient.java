package ch.framedev.marketplace.database;

/*
 * ch.framedev.marketplace.database
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 19:28
 */

import ch.framedev.marketplace.utils.ConfigUtils;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.Collections;

// Require Testing (Not completed)
public class MongoDBClient {

    private MongoClient client;
    private MongoDatabase mongoDatabase;

    /**
     * Constructor for MongoDBClient.
     * This constructor initializes the MongoDB client and connects to the database.
     * It uses either a URI or credentials based on the configuration.
     */
    public MongoDBClient() {
        if (ConfigUtils.MONGODB_USE_URI) {
            connectWithUri();
        } else {
            connectWithCredentials();
        }
        // Check if the connection was successful
        connect();
    }

    private void connectWithUri() {
        if (ConfigUtils.MONGODB_URI != null) {
            client = MongoClients.create(ConfigUtils.MONGODB_URI);
            if(ConfigUtils.MONGODB_DATABASE != null) {
                this.mongoDatabase = client.getDatabase(ConfigUtils.MONGODB_DATABASE);
            } else {
                System.out.println("MongoDB database name is null. Please check your configuration.");
            }
        } else {
            System.out.println("MongoDB URI is null. Please check your configuration.");
        }
    }

    private void connectWithCredentials() {
        if (ConfigUtils.MONGODB_USERNAME != null && ConfigUtils.MONGODB_PASSWORD != null && ConfigUtils.MONGODB_DATABASE != null) {
            String host = ConfigUtils.MONGODB_HOST;
            String username = ConfigUtils.MONGODB_USERNAME;
            String password = ConfigUtils.MONGODB_PASSWORD;
            String hostname = ConfigUtils.MONGODB_HOST;
            int port = ConfigUtils.MONGODB_PORT;
            String dataBaseString = ConfigUtils.MONGODB_DATABASE;
            MongoCredential credential = MongoCredential.createCredential(username, dataBaseString, password.toCharArray());
            this.client = MongoClients.create(
                    MongoClientSettings.builder()
                            .credential(credential)
                            .applyToClusterSettings(builder ->
                                    builder.hosts(Collections.singletonList(new ServerAddress(hostname, port)))).build());
            this.mongoDatabase = client.getDatabase(dataBaseString);
        } else {
            System.out.println("MongoDB credentials or database name is null. Please check your configuration.");
        }
    }

    public void connect() {
        if (client != null) {
            System.out.println("Connected to MongoDB");
        } else {
            System.out.println("Failed to connect to MongoDB");
        }
    }

    public void close() {
        if (client != null) {
            client.close();
        }
    }

    public MongoClient getClient() {
        return client;
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }
}
