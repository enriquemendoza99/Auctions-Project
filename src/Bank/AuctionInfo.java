package Bank;
import java.io.Serializable;

/**
 * this the info needed for the auction, this contains account number
 * and auction id which will be needed in auction package.
 */
public class AuctionInfo implements Serializable {
    int accountNum;
    String auctionID;

    public AuctionInfo(int accountNum, String auctionID) {
        this.auctionID = auctionID;
        this.accountNum = accountNum;
    }

    public int getAccountNum() {
        return accountNum;
    }

    public String getAuctionID() {
        return auctionID;
    }
}
