package ch.framedev.marketplace.transactions;



/*
 * ch.framedev.marketplace.transactions
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 19:31
 */

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Transaction {

    private int id;
    private UUID playerUUID;
    private List<Integer> itemsForSale;
    private List<Integer> itemsSold;
    private List<UUID> receivers;

    public Transaction(UUID playerUUID, List<Integer> itemsForSale, List<Integer> itemsSold, List<UUID> receivers) {
        this.id = new Random().nextInt(0, 100000);
        this.playerUUID = playerUUID;
        this.itemsForSale = itemsForSale;
        this.itemsSold = itemsSold;
        this.receivers = receivers;
    }

    public Transaction(int id, UUID playerUUID, List<Integer> itemsForSale, List<Integer> itemsSold, List<UUID> receivers) {
        this.id = id;
        this.playerUUID = playerUUID;
        this.itemsForSale = itemsForSale;
        this.itemsSold = itemsSold;
        this.receivers = receivers;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public List<Integer> getItemsForSale() {
        return itemsForSale;
    }

    public void setItemsForSale(List<Integer> itemsForSale) {
        this.itemsForSale = itemsForSale;
    }

    public List<Integer> getItemsSold() {
        return itemsSold;
    }

    public void setItemsSold(List<Integer> itemsSold) {
        this.itemsSold = itemsSold;
    }

    public List<UUID> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<UUID> receivers) {
        this.receivers = receivers;
    }

    public List<String> uuidToStringList(List<UUID> uuidList) {
        return uuidList.stream().map(UUID::toString).toList();
    }

    public void addTransaction() {

    }
}
