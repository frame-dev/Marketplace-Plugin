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
import ch.framedev.marketplace.utils.ItemHelper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
                .append("player", sellItem.getPlayerUUID().toString())
                .append("itemStack", sellItem.serializedItemStack())
                .append("amount", sellItem.getAmount())
                .append("price", sellItem.getPrice())
                .append("type", "sell");

        if (documentExists(document)) {
            return false; // Item already exists
        }

        insertDocument(document);
        return true; // Item sold successfully
    }

    public List<SellItem> getAllSellItems() {
        return getCollection().find().filter(new Document("type", "sell")).map(document -> {
            int id = document.getInteger("id");
            UUID playerUUID = UUID.fromString(document.getString("player"));
            ItemStack itemStack = null;
            try {
                itemStack = ItemHelper.fromBase64(document.getString("itemStack"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            itemStack.setAmount(document.getInteger("amount"));
            double price = document.getDouble("price");

            return new SellItem(id, playerUUID, itemStack, price);
        }).into(new ArrayList<>());
    }
}
