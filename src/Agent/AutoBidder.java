package Agent;

import constants.AuctionHouseAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Periodically checks active auctions the agent is tracking and places
 * an incremental bid automatically if the current bid is still below
 * the agent's configured maximum.
 */
public class AutoBidder extends Thread {
    private Agent agent;
    // Keyed by "ip:port|itemName" so each tracked item has its own settings
    private Map<String, Double> maxBids;
    private Map<String, Double> currentBids;
    private Map<String, AuctionHouseAddress> addresses;
    private Map<String, String> itemNames;
    private boolean running;

    public AutoBidder(Agent agent) {
        this.agent = agent;
        this.maxBids = new HashMap<>();
        this.currentBids = new HashMap<>();
        this.addresses = new HashMap<>();
        this.itemNames = new HashMap<>();
        this.running = true;
    }

    /**
     * Registers an item to be auto-bid on up to the given maximum.
     * @param address the auction house address
     * @param itemName the item to track
     * @param maxBid the maximum amount the agent is willing to bid
     */
    public void setMaxBid(AuctionHouseAddress address, String itemName, double maxBid) {
        String key = makeKey(address, itemName);
        maxBids.put(key, maxBid);
        addresses.put(key, address);
        itemNames.put(key, itemName);
    }

    /**
     * Updates the known current bid for a tracked item and automatically
     * places a higher bid if the maximum has not been reached.
     */
    public void updateCurrentBid(AuctionHouseAddress address, String itemName,
                                 double currentBid) {
        String key = makeKey(address, itemName);
        currentBids.put(key, currentBid);
        checkAndPlaceBid(key);
    }

    private void checkAndPlaceBid(String key) {
        Double maxBid = maxBids.get(key);
        Double currentBid = currentBids.get(key);

        if (maxBid != null && currentBid != null && maxBid > currentBid) {
            double newBid = Math.min(maxBid, currentBid + calculateBidIncrement(currentBid));
            agent.placeBid(addresses.get(key), itemNames.get(key), newBid);
        }
    }

    private double calculateBidIncrement(double currentBid) {
        return currentBid * 0.1; // 10% increment
    }

    private String makeKey(AuctionHouseAddress address, String itemName) {
        return address.getIpAddress() + ":" + address.getPortNum() + "|" + itemName;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stopBidding() { running = false; }
}