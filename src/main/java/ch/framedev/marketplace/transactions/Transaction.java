package ch.framedev.marketplace.transactions;



/*
 * ch.framedev.marketplace.transactions
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 19:31
 */

import java.util.*;

public class Transaction {

    private int id;
    private UUID playerUUID;
    private List<Integer> itemsForSale;
    private List<Integer> itemsSold;
    private Map<Integer, UUID> receivers;

    public Transaction(UUID playerUUID, List<Integer> itemsForSale, List<Integer> itemsSold, Map<Integer, UUID> receivers) {
        this.id = new Random().nextInt(0, 100000);
        this.playerUUID = playerUUID;
        this.itemsForSale = itemsForSale;
        this.itemsSold = itemsSold;
        this.receivers = receivers;
    }

    public Transaction(int id, UUID playerUUID, List<Integer> itemsForSale, List<Integer> itemsSold, Map<Integer, UUID> receivers) {
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

    public Map<Integer, UUID> getReceivers() {
        return receivers;
    }

    public void setReceivers(Map<Integer, UUID> receivers) {
        this.receivers = receivers;
    }

    public Map<Integer, String> uuidToStringList(Map<Integer, UUID> uuidList) {
        Map<Integer, String> stringList = new HashMap<>();
        for(Map.Entry<Integer, UUID> entry : uuidList.entrySet()) {
            stringList.put(entry.getKey(), entry.getValue().toString());
        }
        return stringList;
    }

    public void addTransaction() {

    }
}
