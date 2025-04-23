package ch.framedev.marketplace.database;



/*
 * ch.framedev.marketplace.database
 * =============================================
 * This File was Created by FrameDev.
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 19:30
 */

import ch.framedev.marketplace.item.Item;
import ch.framedev.marketplace.transactions.Transaction;
import ch.framedev.marketplace.utils.ConfigUtils;
import ch.framedev.marketplace.utils.ConfigVariables;
import ch.framedev.marketplace.utils.ItemHelper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

// Require Testing (Not completed)
public class DatabaseHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class.getName());

    private final Boolean logging = ConfigVariables.SETTINGS_LOGGING_MONGODB;

    // Client for mongodb connection
    private final MongoDBClient mongoDBClient;
    // Collection name retrieved from config.yml
    private final String collectionName;

    public DatabaseHelper() {
        this.mongoDBClient = new MongoDBClient();
        this.collectionName = ConfigVariables.MONGODB_COLLECTION;
    }

    public MongoDatabase getDatabase() {
        return mongoDBClient.getMongoDatabase();
    }

    public MongoCollection<Document> getCollection() {
        try {
            return mongoDBClient.getMongoDatabase().getCollection(collectionName);
        } catch (Exception e) {
            LOGGER.error("Failed to get MongoDB collection.", e);
        }
        return null;
    }

    public void insertDocument(Document document) {
        try {
            InsertOneResult result = getCollection().insertOne(document);
            if (result.wasAcknowledged()) {
                if (logging)
                    LOGGER.info("Document inserted successfully.");
            } else {
                if (logging)
                    LOGGER.warn("Document insertion was not acknowledged.");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to insert document.", e);
        }
    }

    public void updateDocument(Document filter, Document update) {
        try {
            Document updateWithOperator = new Document("$set", update);
            UpdateResult result = getCollection().updateOne(filter, updateWithOperator);
            if (result.getModifiedCount() > 0) {
                if (logging)
                    LOGGER.info("Document updated successfully.");
            } else {
                if (logging)
                    LOGGER.warn("No documents were updated.");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update document.", e);
        }
    }

    public void deleteDocument(Document filter) {
        try {
            DeleteResult deleteResult = getCollection().deleteOne(filter);
            if (deleteResult.getDeletedCount() > 0) {
                if (logging)
                    LOGGER.info("Document deleted successfully.");
            } else {
                if (logging)
                    LOGGER.warn("No documents were deleted.");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to delete document.", e);
        }
    }

    public boolean documentExists(Document filter) {
        if (getCollection() == null) {
            if (logging)
                LOGGER.error("MongoDB collection is null. Cannot check document existence.");
            return false;
        }
        if (filter == null) {
            if (logging)
                LOGGER.error("Filter is null. Cannot check document existence.");
            return false;
        }
        return getCollection().find(filter).first() != null;
    }

    public boolean sellItem(Item item) {
        Document document = new Document("id", item.getId())
                .append("player", item.getPlayerUUID().toString())
                .append("itemStack", item.serializedItemStack())
                .append("amount", item.getAmount())
                .append("price", item.getPrice())
                .append("type", "sell")
                .append("itemName", item.getName())
                .append("discountPrice", item.getDiscountPrice());

        if (documentExists(document)) {
            return false; // Item already exists
        }

        insertDocument(document);
        if (ConfigVariables.SETTINGS_TRANSACTION_USE_HISTORY) {
            UUID playerUUID = item.getPlayerUUID();
            Transaction transaction = getTransaction(playerUUID)
                    .orElse(new Transaction(playerUUID, new ArrayList<>(), new ArrayList<>(), new HashMap<>()));
            transaction.getItemsForSale().add(item.getId());
            if (transaction.getReceivers() == null)
                transaction.setReceivers(new HashMap<>());
            if (!updateTransaction(transaction)) {
                String error = ConfigVariables.ERROR_UPDATING_TRANSACTION;
                error = ConfigUtils.translateColor(error, "Error updating transaction.");
                error = error.replace("{id}", String.valueOf(transaction.getId()));
                LOGGER.error(error);
                return false;
            }
        }
        return true; // Item sold successfully
    }

    public void removeItem(Item item) {
        if (!documentExists(new Document("id", item.getId())))
            return;
        Document updateDocument = new Document("type", "removed");
        updateDocument(new Document("id", item.getId()), updateDocument);
    }

    public boolean notSoldItem(Item item, Player receiver) {
        Document document = new Document("id", item.getId());
        Document updated = new Document("type", "sold").append("sold", true);

        if (documentExists(document)) {
            updateDocument(document, updated);
        }

        if (ConfigVariables.SETTINGS_TRANSACTION_USE_HISTORY) {
            UUID playerUUID = item.getPlayerUUID();
            Transaction transaction = getTransaction(playerUUID)
                    .orElse(new Transaction(playerUUID, new ArrayList<>(), new ArrayList<>(), new HashMap<>()));
            transaction.getItemsSold().add(item.getId());
            if (transaction.getReceivers() == null)
                transaction.setReceivers(new HashMap<>());
            transaction.getReceivers().put(item.getId(), receiver.getUniqueId());
            if (updateTransaction(transaction)) {
                return false;
            } else {
                String error = ConfigVariables.ERROR_UPDATING_TRANSACTION;
                error = ConfigUtils.translateColor(error, "Error updating transaction.");
                error = error.replace("{id}", String.valueOf(transaction.getId()));
                LOGGER.error(error);
                return true;
            }
        }
        return true; // Item sold successfully
    }

    public List<Item> getAllItemsSoldSell() {
        return getCollection().find().filter(new Document("type", new Document("$in", List.of("sell", "sold")))).map(document -> {
            int id = document.getInteger("id");
            UUID playerUUID = UUID.fromString(document.getString("player"));
            ItemStack itemStack;
            try {
                itemStack = ItemHelper.fromBase64(document.getString("itemStack"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            itemStack.setAmount(document.getInteger("amount"));
            double price = document.getDouble("price");
            boolean sold = document.getBoolean("sold", false);
            boolean discount = document.getBoolean("discount", false);

            String itemName = document.getString("itemName");
            double discountPrice = document.getDouble("discountPrice");

            return new Item(id, playerUUID, itemStack, price, sold, discount, itemName, discountPrice);
        }).into(new ArrayList<>());
    }

    public List<Item> getAllItems() {
        return getCollection().find().filter(new Document("type", "sell")).map(document -> {
            int id = document.getInteger("id");
            UUID playerUUID = UUID.fromString(document.getString("player"));
            ItemStack itemStack;
            try {
                itemStack = ItemHelper.fromBase64(document.getString("itemStack"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            itemStack.setAmount(document.getInteger("amount"));
            double price = document.getDouble("price");
            boolean sold = document.getBoolean("sold", false);
            boolean discount = document.getBoolean("discount", false);
            String itemName = document.getString("itemName");
            double discountPrice = document.getDouble("discountPrice");

            return new Item(id, playerUUID, itemStack, price, sold, discount, itemName, discountPrice);
        }).into(new ArrayList<>());
    }

    @SuppressWarnings("unused")
    public List<Item> getAllSoldItems() {
        return getCollection().find().filter(new Document("type", "sold")).map(document -> {
            int id = document.getInteger("id");
            UUID playerUUID = UUID.fromString(document.getString("player"));
            ItemStack itemStack;
            try {
                itemStack = ItemHelper.fromBase64(document.getString("itemStack"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            itemStack.setAmount(document.getInteger("amount"));
            double price = document.getDouble("price");
            boolean sold = document.getBoolean("sold", false);
            boolean discount = document.getBoolean("discount", false);
            String itemName = document.getString("itemName");
            double discountPrice = document.getDouble("discountPrice");

            return new Item(id, playerUUID, itemStack, price, sold, discount, itemName, discountPrice);
        }).into(new ArrayList<>());
    }

    @SuppressWarnings("unused")
    public UUID getPlayerReceiver(int itemId) {
        List<Transaction> transactions = getAllTransactions();
        for (Transaction transaction : transactions) {
            for (int id : transaction.getItemsSold()) {
                if (id == itemId) {
                    return transaction.getReceivers().get(id);
                }
            }
        }
        return null;
    }

    public Item getItem(int id) {
        if (!documentExists(new Document("id", id).append("type", "sell"))) return null;
        Document document = getCollection().find().filter(new Document("id", id).append("type", "sell")).first();
        if (document == null) return null;
        UUID playerUUID = UUID.fromString(document.getString("player"));
        ItemStack itemStack;
        try {
            itemStack = ItemHelper.fromBase64(document.getString("itemStack"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        itemStack.setAmount(document.getInteger("amount"));
        double price = document.getDouble("price");
        boolean sold = document.getBoolean("sold", false);
        boolean discount = document.getBoolean("discount", false);
        String itemName = document.getString("itemName");
        double discountPrice = document.getDouble("discountPrice");

        return new Item(id, playerUUID, itemStack, price, sold, discount, itemName, discountPrice);
    }

    public Item getItemByName(String itemName) {
        if (!documentExists(new Document("itemName", itemName).append("type", new Document("$in", List.of("sell", "sold"))))) {
            for (Item item : getAllItemsSoldSell()) {
                if (item.getName().equalsIgnoreCase(itemName)) {
                    return item;
                }
            }
            return null;
        }
        return getCollection().find(new Document("itemName", itemName).append("type", new Document("$in", List.of("sell", "sold")))).map(doc -> {
            int id = doc.getInteger("id");
            UUID playerUUID = UUID.fromString(doc.getString("player"));
            ItemStack itemStack;
            try {
                itemStack = ItemHelper.fromBase64(doc.getString("itemStack"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            itemStack.setAmount(doc.getInteger("amount"));
            double price = doc.getDouble("price");
            boolean sold = doc.getBoolean("sold", false);
            boolean discount = doc.getBoolean("discount", false);
            double discountPrice = doc.getDouble("discountPrice");

            return new Item(id, playerUUID, itemStack, price, sold, discount, itemName, discountPrice);
        }).into(new ArrayList<>()).getFirst();
    }

    public Item getTypeItem(int id) {
        if (!documentExists(new Document("id", id).append("type", new Document("$in", List.of("sell", "sold"))))) {
            System.out.println("Does not exists! " + id);
            return null;
        }
        Document document = getCollection().find().filter(new Document("id", id).append("type", new Document("$in", List.of("sell", "sold")))).first();
        if (document == null) return null;
        UUID playerUUID = UUID.fromString(document.getString("player"));
        ItemStack itemStack;
        try {
            itemStack = ItemHelper.fromBase64(document.getString("itemStack"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        itemStack.setAmount(document.getInteger("amount"));
        double price = document.getDouble("price");
        boolean sold = document.getBoolean("sold", false);
        boolean discount = document.getBoolean("discount", false);
        String itemName = document.getString("itemName");
        double discountPrice = document.getDouble("discountPrice");

        return new Item(id, playerUUID, itemStack, price, sold, discount, itemName, discountPrice);
    }

    public List<Item> getItemsByPlayer(UUID playerUUID) {
        return getCollection()
                .find(new Document("player", playerUUID.toString())
                        .append("type", new Document("$in", List.of("sell", "sold"))))
                .map(doc -> getTypeItem(doc.getInteger("id")))
                .into(new ArrayList<>());
    }

    public int discountItemSize() {
        return getCollection().find().filter(new Document("type", "sell").append("discount", true)).into(new ArrayList<>()).size();
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean updateSellItem(Item item) {
        Document document = new Document("id", item.getId())
                .append("player", item.getPlayerUUID().toString())
                .append("itemStack", item.serializedItemStack())
                .append("amount", item.getAmount())
                .append("price", item.getPrice())
                .append("discount", item.isDiscount())
                .append("itemName", item.getName())
                .append("discountPrice", item.getDiscountPrice())
                .append("type", "sell");
        if (!documentExists(new Document("id", item.getId()).append("type", "sell")))
            return false;
        updateDocument(new Document("id", item.getId()).append("type", "sell"), document);
        return true;
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
                .append("receivers", new Gson().toJson(transaction.uuidToStringList(transaction.getReceivers())))
                .append("type", "transaction");

        insertDocument(document);
        return true;
    }

    public boolean updateTransaction(Transaction transaction) {
        try {
            Document filter = new Document("id", transaction.getId()).append("type", "transaction");
            if (!documentExists(filter)) {
                if (!addTransaction(transaction)) {
                    String error = ConfigVariables.ERROR_ADD_TRANSACTION;
                    error = ConfigUtils.translateColor(error, "§cError adding transaction! {id}");
                    error = error.replace("{id}", String.valueOf(transaction.getId()));
                    LOGGER.error(error);
                }
                return true;
            }

            Document update = new Document("playerUUID", transaction.getPlayerUUID().toString())
                    .append("itemsForSale", transaction.getItemsForSale())
                    .append("itemsSold", transaction.getItemsSold())
                    .append("receivers", new Gson().toJson(transaction.uuidToStringList(transaction.getReceivers())));

            updateDocument(filter, update);
            return true;
        } catch (Exception ex) {
            String error = ConfigVariables.ERROR_UPDATING_TRANSACTION;
            error = ConfigUtils.translateColor(error, "§cError updating transaction.");
            error = error.replace("{id}", String.valueOf(transaction.getId()));
            LOGGER.error(error);
            return false;
        }
    }

    public List<Transaction> getAllTransactions() {
        return getCollection().find(new Document("type", "transaction")).map(document -> {
            int id = document.getInteger("id");
            UUID uuid = UUID.fromString(document.getString("playerUUID"));
            List<Integer> itemsForSale = document.getList("itemsForSale", Integer.class);
            List<Integer> itemsSold = document.getList("itemsSold", Integer.class);

            Type type = new TypeToken<Map<Integer, String>>() {
            }.getType();
            Map<Integer, String> receivers = new Gson().fromJson(document.getString("receivers"), type);
            if (receivers == null)
                return new Transaction(id, uuid, itemsForSale, itemsSold, new HashMap<>());
            Map<Integer, UUID> receiversUUID = new HashMap<>();
            for (Map.Entry<Integer, String> entry : receivers.entrySet()) {
                receiversUUID.put(entry.getKey(), UUID.fromString(entry.getValue()));
            }

            return new Transaction(id, uuid, itemsForSale, itemsSold, receiversUUID);
        }).into(new ArrayList<>());
    }

    public Optional<Transaction> getTransaction(UUID playerUUID) {
        Document document = new Document("playerUUID", playerUUID.toString()).append("type", "transaction");
        if (!documentExists(document)) {
            return Optional.empty();
        }
        Document found = getCollection().find(document).first();
        if (found == null) {
            return Optional.empty();
        }
        int id = found.getInteger("id");
        List<Integer> itemsForSale = new ArrayList<>(found.getList("itemsForSale", Integer.class));
        List<Integer> itemsSold = new ArrayList<>(found.getList("itemsSold", Integer.class));
        Type type = new TypeToken<Map<Integer, String>>() {
        }.getType();
        Map<Integer, String> receivers = new Gson().fromJson(found.getString("receivers"), type);
        if (receivers == null) {
            return Optional.of(new Transaction(id, playerUUID, itemsForSale, itemsSold, new HashMap<>()));
        }
        Map<Integer, UUID> receiversUUID = new HashMap<>();
        for (Map.Entry<Integer, String> entry : receivers.entrySet()) {
            receiversUUID.put(entry.getKey(), UUID.fromString(entry.getValue()));
        }

        return Optional.of(new Transaction(id, playerUUID, itemsForSale, itemsSold, receiversUUID));
    }
}
