package AuctionHouse;

import java.io.Serializable;

public class Item implements Serializable {
    private String name;
    private double startingPrice;
    private String description;

    public Item(String name, double startingPrice, String description) {
        this.name = name;
        this.startingPrice = startingPrice;
        this.description = description;
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
}