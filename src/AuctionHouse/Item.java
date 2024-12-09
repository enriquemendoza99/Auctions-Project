package AuctionHouse;

import java.io.Serializable;

public class Item implements Serializable {
    private String name;
    private double startingPrice;
    private String description;
    private double currentBid;
    private int currentBidder;

    public Item(String name, double startingPrice, String description) {
        this.name = name;
        this.startingPrice = startingPrice;
        this.description = description;
        this.currentBid = startingPrice;
    }

    public String getName() {
        return name;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public String getDescription() {
        return description;
    }

    public double getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(double currentBid, int bidder) {
        this.currentBid = currentBid;
        this.currentBidder = bidder;
    }

    @Override
    public String toString() {
        return name + " - Current bid: $" + currentBid +
                " (Starting price: $" + startingPrice + ")\n" +
                "Description: " + description;
    }
}