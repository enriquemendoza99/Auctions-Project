package Agent;

import java.util.HashMap;
import java.util.Map;

public class AutoBidder extends Thread {
    private Agent agent;
    private Map<String, Double> maxBids;
    private Map<String, Double> currentBids;
    private boolean running;

    public AutoBidder(Agent agent) {
        this.agent = agent;
        this.maxBids = new HashMap<>();
        this.currentBids = new HashMap<>();
        this.running = true;
    }

    public void setMaxBid(String auctionId, double maxBid) {
        maxBids.put(auctionId, maxBid);
    }

    public void updateCurrentBid(String auctionId, double currentBid) {
        currentBids.put(auctionId, currentBid);
        checkAndPlaceBid(auctionId);
    }

    private void checkAndPlaceBid(String auctionId) {
        Double maxBid = maxBids.get(auctionId);
        Double currentBid = currentBids.get(auctionId);

        if (maxBid != null && currentBid != null && maxBid > currentBid) {
            double newBid = Math.min(maxBid, currentBid + calculateBidIncrement(currentBid));
            agent.placeBid(auctionId, newBid);
        }
    }

    private double calculateBidIncrement(double currentBid) {
        // Simple increment calculation
        return currentBid * 0.1; // 10% increment
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(1000); // Check every second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stopBidding() {
        running = false;
    }
}