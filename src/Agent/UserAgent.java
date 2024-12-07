package Agent;

import constants.Message;
import constants.StatusBid;
import java.io.*;
import java.util.Scanner;

public class UserAgent extends Agent {
    private final Scanner scanner;
    private volatile boolean running;

    public UserAgent(String name, double initialBalance) {
        super(name, initialBalance);
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    @Override
    public void run() {
        try {
            System.out.println("Enter bank host:");
            String bankHost = scanner.nextLine();
            System.out.println("Enter bank port:");
            int bankPort = Integer.parseInt(scanner.nextLine());

            connectToBank(bankHost, bankPort);
            updateAuctionHouses();

            while (running) {
                showMenu();
                processUserInput();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void showMenu() {
        System.out.println("\n=== Agent Menu ===");
        System.out.println("1. View Balance");
        System.out.println("2. View Auctions");
        System.out.println("3. Place Bid");
        System.out.println("4. Refresh Auction Houses");
        System.out.println("5. Exit");
        System.out.print("Choose option: ");
    }

    private void processUserInput() {
        String choice = scanner.nextLine();
        try {
            switch (choice) {
                case "1":
                    System.out.println("Current balance: $" + getBalance());
                    break;
                case "2":
                    viewAuctions();
                    break;
                case "3":
                    placeBidMenu();
                    break;
                case "4":
                    updateAuctionHouses();
                    break;
                case "5":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option");
            }
        } catch (Exception e) {
            System.out.println("Error processing command: " + e.getMessage());
        }
    }

    private void viewAuctions() {
        for (String auctionKey : auctionOutputs.keySet()) {
            try {
                ObjectOutputStream out = auctionOutputs.get(auctionKey);
                out.writeObject(new Message("GetItems"));
            } catch (IOException e) {
                System.out.println("Error getting items from " + auctionKey);
            }
        }
    }

    private void placeBidMenu() throws IOException {
        System.out.println("Enter auction address (host:port):");
        String auctionKey = scanner.nextLine();
        System.out.println("Enter item ID:");
        String itemId = scanner.nextLine();
        System.out.println("Enter bid amount:");
        double amount = Double.parseDouble(scanner.nextLine());

        placeBid(auctionKey, itemId, amount);
    }

    @Override
    protected void handleAuctionMessage(Message message, String auctionKey) {
        switch (message.getCommand()) {
            case "ItemList":
                System.out.println("\nItems at " + auctionKey + ":");
                @SuppressWarnings("unchecked")
                java.util.List<AuctionHouse.AuctionItem> items =
                        (java.util.List<AuctionHouse.AuctionItem>) message.splitCommand(1);
                for (AuctionHouse.AuctionItem item : items) {
                    System.out.printf("ID: %s, %s, Current bid: $%.2f%n",
                            item.getItemId(), item.getDescription(), item.getCurrentBid());
                }
                break;

            case "BidResult":
                StatusBid status = (StatusBid) message.splitCommand(1);
                System.out.println("Bid result: " + status);
                break;

            case "BidUpdate":
                String itemId = (String) message.splitCommand(1);
                double newBid = (Double) message.splitCommand(2);
                int bidderId = (Integer) message.splitCommand(3);
                System.out.printf("New bid on item %s: $%.2f by agent %d%n",
                        itemId, newBid, bidderId);
                break;

            case "Winner":
                System.out.println("You won item " + message.splitCommand(1) +
                        " for $" + message.splitCommand(2));
                break;
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java UserAgent <name> <initial-balance>");
            System.exit(1);
        }

        String name = args[0];
        double balance = Double.parseDouble(args[1]);

        UserAgent agent = new UserAgent(name, balance);
        agent.start();
    }
}
