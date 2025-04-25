package ch.framedev.marketplace.transactions;



/*
 * ch.framedev.marketplace.transactions
 * =============================================
 * This File was Created by FrameDev.
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 19:31
 */

import java.util.*;

public class Transaction {

    private UUID id;
    private final UUID playerUUID;
    private final List<UUID> itemsForSale;
    private final List<UUID> itemsSold;
    private Map<UUID, UUID> itemBought;
    private Map<UUID, UUID> receivers;

    public Transaction(UUID playerUUID, List<UUID> itemsForSale, List<UUID> itemsSold, Map<UUID, UUID> receivers, Map<UUID, UUID> itemBought) {
        this.id = UUID.randomUUID();
        this.playerUUID = playerUUID;
        this.itemsForSale = itemsForSale;
        this.itemsSold = itemsSold;
        this.receivers = receivers;
        this.itemBought = itemBought;
    }

    public Transaction(UUID id, UUID playerUUID, List<UUID> itemsForSale, List<UUID> itemsSold, Map<UUID, UUID> receivers, Map<UUID, UUID> itemBought) {
        this.id = id;
        this.playerUUID = playerUUID;
        this.itemsForSale = itemsForSale;
        this.itemsSold = itemsSold;
        this.receivers = receivers;
        this.itemBought = itemBought;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public List<UUID> getItemsForSale() {
        return itemsForSale;
    }

    public List<UUID> getItemsSold() {
        return itemsSold;
    }

    public Map<UUID, UUID> getReceivers() {
        return receivers;
    }

    public void setReceivers(Map<UUID, UUID> receivers) {
        this.receivers = receivers;
    }

    public Map<UUID, UUID> getItemBought() {
        return itemBought;
    }

    public void setItemBought(Map<UUID, UUID> itemBought) {
        this.itemBought = itemBought;
    }

    public Map<String, String> uuidToStringList(Map<UUID, UUID> uuidList) {
        Map<String, String> stringList = new HashMap<>();
        for(Map.Entry<UUID, UUID> entry : uuidList.entrySet()) {
            stringList.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return stringList;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Transaction that)) return false;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getPlayerUUID(), that.getPlayerUUID()) && Objects.equals(getItemsForSale(), that.getItemsForSale()) && Objects.equals(getItemsSold(), that.getItemsSold()) && Objects.equals(getReceivers(), that.getReceivers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getPlayerUUID(), getItemsForSale(), getItemsSold(), getReceivers());
    }
}
