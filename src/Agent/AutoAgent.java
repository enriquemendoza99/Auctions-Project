package Agent;

import constants.Message;
import constants.StatusBid;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class AutoAgent extends Agent {
    private final Random random;
    private final ScheduledExecutorService bidScheduler;
    private final Map<String, List<String>> availableItems;
    private volatile boolean running;

    public AutoAgent(String name, double initialBalance) {
        super(name, initialBalance);
        this.random = new Random();
        this.bidScheduler = Executors.newScheduledThreadPool(1);
        this.availableItems = new ConcurrentHashMap<>();
        this.running = true;
    }

    @Override
    public void run() {
        try {
            // Get bank connection details - could be passed through constructor or configuration
            System.out.println("Auto Agent connecting to bank...");
            String bankHost = "localhost"; // Can be configured as needed
            int bankPort = 8080;          // Can be configured as needed

            connectToBank(bankHost, bankPort);
            updateAuctionHouses();

            // Schedule periodic bidding attempts
            bidScheduler.scheduleAtFixedRate(
                    this::attemptRandomBid,
                    5,  // Initial delay
                    10, // Period between bids
                    TimeUnit.SECONDS
            );

            // Periodically refresh auction house list
            bidScheduler.scheduleAtFixedRate(
                    this::refreshAuctionHouses,
                    30,
                    60,
                    TimeUnit.SECONDS
            );

            while (running) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void refreshAuctionHouses() {
        try {
            updateAuctionHouses();
            // Request item lists from all auction houses
            for (String auctionKey : auctionOutputs.keySet()) {
                auctionOutputs.get(auctionKey).writeObject(new Message("GetItems"));
            }
        } catch (Exception e) {
            System.out.println("Error refreshing auction houses: " + e.getMessage());
        }
    }

    private void attemptRandomBid() {
        try {
            // Check if we have any items to bid on
            if (availableItems.isEmpty()) {
                return;
            }

            // Randomly select an auction house
            List<String> auctionKeys = new ArrayList<>(availableItems.keySet());
            String selectedAuction = auctionKeys.get(random.nextInt(auctionKeys.size()));

            // Randomly select an item
            List<String> items = availableItems.get(selectedAuction);
            if (items.isEmpty()) {
                return;
            }
            String selectedItem = items.get(random.nextInt(items.size()));

            // Get current balance
            double balance = getBalance();

            // Generate random bid amount (50-90% of available balance)
            double bidPercent = 0.5 + (random.nextDouble() * 0.4);
            double bidAmount = balance * bidPercent;

            // Place bid
            placeBid(selectedAuction, selectedItem, bidAmount);

        } catch (Exception e) {
            System.out.println("Error attempting random bid: " + e.getMessage());
        }
    }

    @Override
    protected void handleAuctionMessage(Message message, String auctionKey) {
        switch (message.getCommand()) {
            case "ItemList":
                @SuppressWarnings("unchecked")
                java.util.List<AuctionHouse.AuctionItem> items =
                        (java.util.List<AuctionHouse.AuctionItem>) message.splitCommand(1);

                List<String> itemIds = new ArrayList<>();
                for (AuctionHouse.AuctionItem item : items) {
                    if (item.isAvailable()) {
                        itemIds.add(item.getItemId());
                    }
                }
                availableItems.put(auctionKey, itemIds);
                break;

            case "BidResult":
                StatusBid status = (StatusBid) message.splitCommand(1);
                System.out.printf("Bid result for %s: %s%n", auctionKey, status);
                break;

            case "Winner":
                System.out.printf("Won auction at %s for item %s at $%.2f%n",
                        auctionKey, message.splitCommand(1), message.splitCommand(2));
                break;
        }
    }

    @Override
    protected void cleanup() {
        running = false;
        bidScheduler.shutdown();
        super.cleanup();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java AutoAgent <name> <initial-balance>");
            System.exit(1);
        }

        String name = args[0];
        double balance = Double.parseDouble(args[1]);

        AutoAgent agent = new AutoAgent(name, balance);
        agent.start();
    }
}
