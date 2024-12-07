package AuctionHouse;

import constants.StatusBid;
import java.io.Serializable;

public class AuctionItem implements Serializable {
    private String itemId;
    private String description;
    private double minimumBid;
    private double currentBid;
    private int currentBidderId;
    private boolean available;
    private long lastBidTime;

    public AuctionItem(String itemId, String description, double minimumBid) {
        this.itemId = itemId;
        this.description = description;
        this.minimumBid = minimumBid;
        this.currentBid = minimumBid;
        this.available = true;
    }

    public synchronized StatusBid placeBid(double amount, int bidderId) {
        if (!available) {
            return StatusBid.ITEMSOLD;
        }
        if (amount <= currentBid) {
            return StatusBid.BIDLOWER;
        }

        currentBid = amount;
        currentBidderId = bidderId;
        lastBidTime = System.currentTimeMillis();
        return StatusBid.ACCEPTED;
    }

    public String getItemId() { return itemId; }
    public String getDescription() { return description; }
    public double getCurrentBid() { return currentBid; }
    public double getMinimumBid() { return minimumBid; }
    public int getCurrentBidderId() { return currentBidderId; }
    public boolean isAvailable() { return available; }
    public long getLastBidTime() { return lastBidTime; }

    public void markAsSold() {
        this.available = false;
    }
}

