package constants;

import java.io.Serializable;

/**
 * this object is used just to store the address of the auction house.
 * These addresses can be accessed by other programs.
 */
public class AuctionHouseAddress implements Serializable {
    private String ipAddress;
    private int portNum;

    public AuctionHouseAddress(String ipAddress, int portNum) {
        this.ipAddress = ipAddress;
        this.portNum = portNum;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPortNum() {
        return portNum;
    }
}
