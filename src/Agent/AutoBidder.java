package Agent;

import java.util.Random;

public class AutoBidder {
    private Agent agent;
    private Random random;

    public AutoBidder(Agent agent) {
        this.agent = agent;
        this.random = new Random();
    }

    public void start() {
        new Thread(this::run).start();
    }

    private void run() {
        while (agent.isRunning()) {
            try {
                Thread.sleep(5000); // Wait 5 seconds between bids
                agent.getAuctionManager().viewItems();
                // Auto bidding logic here
            } catch (Exception e) {
                if (agent.isRunning()) e.printStackTrace();
            }
        }
    }
}
