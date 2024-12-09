package Bank;

import java.io.Serializable;
import java.util.List;
import AuctionHouse.Item;

public class AuctionInfo implements Serializable {
    private int accountNum;
    private String auctionID;
    private List<Item> items;

    public AuctionInfo(int accountNum, String auctionID) {
        this.accountNum = accountNum;
        this.auctionID = auctionID;
    }

    public int getAccountNum() {
        return accountNum;
    }

    public String getAuctionID() {
        return auctionID;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}