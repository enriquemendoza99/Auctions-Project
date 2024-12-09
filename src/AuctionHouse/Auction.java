package AuctionHouse;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Auction {
    private final String auctionId;
    private final Item item;
    private final AtomicReference<Double> currentBid;
    private int currentBidder;
    private boolean active;

    public Auction(Item item) {
        this.auctionId = UUID.randomUUID().toString();
        this.item = item;
        this.currentBid = new AtomicReference<>(item.getStartingPrice());
        this.active = true;
    }

    public synchronized boolean placeBid(int accountNum, double amount) {
        if (!active || amount <= currentBid.get()) {
            return false;
        }

        currentBid.set(amount);
        currentBidder = accountNum;
        item.setCurrentBid(amount, accountNum);
        return true;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public Item getItem() {
        return item;
    }

    public double getCurrentBid() {
        return currentBid.get();
    }

    public int getCurrentBidder() {
        return currentBidder;
    }

    public boolean isActive() {
        return active;
    }

    public void closeAuction() {
        this.active = false;
    }
}
