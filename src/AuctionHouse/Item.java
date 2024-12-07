package AuctionHouse;

import java.io.Serializable;
import java.util.UUID;

public class Item implements Serializable {
    private String id;
    private String name;
    private String description;
    private int minimumBid;
    private int currentBid;
    private int currentBidder;
    private boolean sold;

    public Item(String name, String description, int minimumBid) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.minimumBid = minimumBid;
        this.currentBid = minimumBid;
        this.sold = false;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getMinimumBid() { return minimumBid; }
    public int getCurrentBid() { return currentBid; }
    public void setCurrentBid(int bid) { this.currentBid = bid; }
    public int getCurrentBidder() { return currentBidder; }
    public void setCurrentBidder(int bidder) { this.currentBidder = bidder; }
    public boolean isSold() { return sold; }
    public void setSold(boolean sold) { this.sold = sold; }

    @Override
    public String toString() {
        return String.format("Item: %s (ID: %s) - Current bid: $%d", name, id, currentBid);
    }
}