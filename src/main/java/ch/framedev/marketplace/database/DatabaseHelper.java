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
import ch.framedev.marketplace.transactions.Transaction;
import ch.framedev.marketplace.utils.ConfigUtils;
import ch.framedev.marketplace.utils.ConfigVariables;
import ch.framedev.marketplace.utils.ItemHelper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Require Testing (Not completed)
public class DatabaseHelper {

    // Client for mongodb connection
    private final MongoDBClient mongoDBClient;
    // Collection name retrieved from config.yml
    private final String collectionName;

    public DatabaseHelper() {
        this.mongoDBClient = new MongoDBClient();
        this.collectionName = ConfigVariables.MONGODB_COLLECTION;
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
        Document updateWithOperator = new Document("$set", update);
        getCollection().updateOne(filter, updateWithOperator);
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
        UUID playerUUID = sellItem.getPlayerUUID();
        Transaction transaction = getTransaction(playerUUID)
                .orElse(new Transaction(playerUUID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        transaction.getItemsForSale().add(sellItem.getId());
        if (!updateTransaction(transaction)) {
            String error = ConfigVariables.ERROR_UPDATING_TRANSACTION;
            error = ConfigUtils.translateColor(error, "§cError updating transaction.");
            error = error.replace("{id}", String.valueOf(transaction.getId()));
            System.err.println(error);
            return false;
        }
        return true; // Item sold successfully
    }

    public boolean soldItem(SellItem sellItem, Player receiver) {
        Document document = new Document("id", sellItem.getId());
        Document updated = new Document("type", "sold").append("sold", true);

        if (documentExists(document)) {
            updateDocument(document, updated);
        }

        UUID playerUUID = sellItem.getPlayerUUID();
        Transaction transaction = getTransaction(playerUUID)
                .orElse(new Transaction(playerUUID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        transaction.getItemsSold().add(sellItem.getId());
        transaction.getReceivers().add(receiver.getUniqueId());
        if (!updateTransaction(transaction)) {
            String error = ConfigVariables.ERROR_UPDATING_TRANSACTION;
            error = ConfigUtils.translateColor(error, "§cError updating transaction.");
            error = error.replace("{id}", String.valueOf(transaction.getId()));
            System.err.println(error);
            return false;
        }
        return true;
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
            boolean sold = document.getBoolean("sold", false);

            return new SellItem(id, playerUUID, itemStack, price, sold);
        }).into(new ArrayList<>());
    }

    public boolean addTransaction(Transaction transaction) {
        Document filter = new Document("id", transaction.getId());
        if (documentExists(filter)) {
            return false; // Transaction already exists
        }

        Document document = new Document("id", transaction.getId())
                .append("playerUUID", transaction.getPlayerUUID().toString())
                .append("itemsForSale", transaction.getItemsForSale())
                .append("itemsSold", transaction.getItemsSold())
                .append("receivers", transaction.uuidToStringList(transaction.getReceivers()));

        insertDocument(document);
        return true;
    }

    public boolean updateTransaction(Transaction transaction) {
        try {
            Document filter = new Document("id", transaction.getId());
            if (!documentExists(filter)) {
                if(!addTransaction(transaction)) {
                    String error = ConfigVariables.ERROR_ADD_TRANSACTION;
                    error = ConfigUtils.translateColor(error, "§cError adding transaction! {id}");
                    error = error.replace("{id}", String.valueOf(transaction.getId()));
                    System.err.println(error);
                }
                return true;
            }

            Document update = new Document("playerUUID", transaction.getPlayerUUID().toString())
                    .append("itemsForSale", transaction.getItemsForSale())
                    .append("itemsSold", transaction.getItemsSold())
                    .append("receivers", transaction.uuidToStringList(transaction.getReceivers()));

            updateDocument(filter, update);
            return true;
        } catch (Exception ex) {
            String error = ConfigVariables.ERROR_UPDATING_TRANSACTION;
            error = ConfigUtils.translateColor(error, "§cError updating transaction.");
            error = error.replace("{id}", String.valueOf(transaction.getId()));
            System.err.println(error);
            return false;
        }
    }

    public Optional<Transaction> getTransaction(UUID playerUUID) {
        Document document = new Document("playerUUID", playerUUID.toString());
        if(!documentExists(document)) {
            return Optional.empty();
        }
        Document found = getCollection().find(document).first();
        int id = document.getInteger("id");
        List<Integer> itemsForSale = document.getList("itemsForSale", Integer.class);
        List<Integer> itemsSold = document.getList("itemsSold", Integer.class);
        List<UUID> receivers = document.getList("receivers", String.class).stream().map(UUID::fromString).toList();

        return Optional.of(new Transaction(id, playerUUID, itemsForSale, itemsSold, receivers));
    }
}
