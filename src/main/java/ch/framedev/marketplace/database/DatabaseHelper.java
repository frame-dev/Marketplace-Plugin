package ch.framedev.marketplace.database;



/*
 * ch.framedev.marketplace.database
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 19:30
 */

import ch.framedev.marketplace.sell.SellItem;
import ch.framedev.marketplace.utils.ConfigUtils;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

// Require Testing (Not completed)
public class DatabaseHelper {

    private final MongoDBClient mongoDBClient;
    private final String collectionName;

    public DatabaseHelper() {
        this.mongoDBClient = new MongoDBClient();
        this.collectionName = ConfigUtils.MONGODB_COLLECTION;
    }

    public MongoDBClient getMongoDBClient() {
        return mongoDBClient;
    }

    public MongoClient getClient() {
        return mongoDBClient.getClient();
    }

    public MongoDatabase getDatabase() {
        return mongoDBClient.getMongoDatabase();
    }

    public MongoCollection<Document> getCollection() {
        return mongoDBClient.getMongoDatabase().getCollection(collectionName);
    }

    public void close() {
        mongoDBClient.close();
    }

    public void insertDocument(Document document) {
        getCollection().insertOne(document);
    }

    public void updateDocument(Document filter, Document update) {
        getCollection().updateOne(filter, update);
    }

    public void deleteDocument(Document filter) {
        getCollection().deleteOne(filter);
    }

    public boolean documentExists(Document filter) {
        return getCollection().find(filter).first() != null;
    }

    public boolean sellItem(SellItem sellItem) {
        Document document = new Document("id", sellItem.getId())
                .append("player", sellItem.getPlayer().getName())
                .append("itemStack", sellItem.serializedItemStack())
                .append("amount", sellItem.getAmount())
                .append("price", sellItem.getPrice());

        if (documentExists(document)) {
            return false; // Item already exists
        }

        insertDocument(document);
        return true; // Item sold successfully
    }
}
