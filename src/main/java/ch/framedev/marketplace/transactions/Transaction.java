package ch.framedev.marketplace.transactions;



/*
 * ch.framedev.marketplace.transactions
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 19:31
 */

import ch.framedev.marketplace.sell.SellItem;

import java.util.List;
import java.util.UUID;

public class Transaction {

    private int id;
    private UUID playerUUID;
    private List<SellItem> itemForSale;
    private List<SellItem> itemsSold;
}
