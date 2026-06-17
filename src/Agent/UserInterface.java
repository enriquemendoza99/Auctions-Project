package Agent;

import constants.AuctionHouseAddress;
import Bank.AuctionInfo;
import AuctionHouse.Item;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.List;

/**
 * Console-based menu for the agent. Lets the user check balance, browse
 * auctions across all registered auction houses, and place bids on items.
 */
public class UserInterface extends Thread {
    private Agent agent;
    private Scanner scanner;
    private boolean running;
    // Cache of the last fetched auction list, keyed by auction house address text
    private HashMap<Object, AuctionHouseAddress> lastAuctions;

    public UserInterface(Agent agent) {
        this.agent = agent;
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    @Override
    public void run() {
        System.out.println("\nStarting user interface...");
        while (running) {
            displayMenu();
            String choice = scanner.nextLine();
            processChoice(choice);
        }
    }

    private void displayMenu() {
        System.out.println("\n=== Agent Menu ===");
        System.out.println("1. View Balance");
        System.out.println("2. View Available Auctions");
        System.out.println("3. Place Bid");
        System.out.println("4. Set Auto-Bid");
        System.out.println("5. Exit");
        System.out.print("Enter choice: ");
    }

    private void processChoice(String choice) {
        try {
            switch (choice) {
                case "1": System.out.println("Current Balance: $" + agent.getBalance()); break;
                case "2": viewAvailableAuctions(); break;
                case "3": placeBid(); break;
                case "4": setAutoBid(); break;
                case "5": System.out.println("Exiting..."); running = false; break;
                default:  System.out.println("Invalid choice. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error processing choice: " + e.getMessage());
        }
    }

    private void viewAvailableAuctions() {
        try {
            System.out.println("\nFetching available auctions...");
            lastAuctions = agent.getAvailableAuctions();

            if (lastAuctions.isEmpty()) {
                System.out.println("No auctions currently available.");
                return;
            }

            System.out.println("\nAvailable Auctions:");
            System.out.println("-------------------");
            int index = 1;
            for (Map.Entry<Object, AuctionHouseAddress> entry : lastAuctions.entrySet()) {
                AuctionHouseAddress address = entry.getValue();
                Object key = entry.getKey();

                if (key instanceof AuctionInfo) {
                    AuctionInfo info = (AuctionInfo) key;
                    System.out.println("\n[" + index + "] Auction House ID: "
                            + info.getAuctionID());
                    System.out.println("    Address: " + address.getIpAddress()
                            + ":" + address.getPortNum());

                    List<Item> items = info.getItems();
                    if (items != null && !items.isEmpty()) {
                        System.out.println("    Items:");
                        for (Item item : items) {
                            System.out.println("      - " + item.getName()
                                    + " | Current bid: $" + item.getCurrentBid()
                                    + " | " + item.getDescription());
                        }
                    }
                }
                System.out.println("-------------------");
                index++;
            }
        } catch (Exception e) {
            System.out.println("Error fetching auctions: " + e.getMessage());
        }
    }

    /**
     * Prompts the user for an auction house address and item name,
     * then places a bid through the agent.
     */
    private void placeBid() {
        try {
            viewAvailableAuctions();

            System.out.print("\nEnter auction house address (IP:Port): ");
            String[] addressParts = scanner.nextLine().split(":");
            if (addressParts.length != 2) {
                System.out.println("Invalid address format. Use IP:Port.");
                return;
            }
            AuctionHouseAddress address;
            try {
                address = new AuctionHouseAddress(addressParts[0],
                        Integer.parseInt(addressParts[1]));
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number.");
                return;
            }

            System.out.print("Enter item name to bid on: ");
            String itemName = scanner.nextLine();

            System.out.print("Enter amount to bid: $");
            try {
                double amount = Double.parseDouble(scanner.nextLine());
                if (amount <= 0) {
                    System.out.println("Bid amount must be positive.");
                    return;
                }
                if (amount > agent.getBalance()) {
                    System.out.println("Insufficient funds. Balance: $"
                            + agent.getBalance());
                    return;
                }
                System.out.println("Placing bid of $" + amount + " on " + itemName + "...");
                agent.placeBid(address, itemName, amount);
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a number.");
            }
        } catch (Exception e) {
            System.out.println("Error placing bid: " + e.getMessage());
        }
    }

    private void setAutoBid() {
        try {
            viewAvailableAuctions();
            System.out.print("\nEnter auction house address (IP:Port): ");
            String auctionAddress = scanner.nextLine();

            System.out.print("Enter maximum bid amount: $");
            try {
                double maxBid = Double.parseDouble(scanner.nextLine());
                if (maxBid <= 0) {
                    System.out.println("Maximum bid amount must be positive.");
                    return;
                }
                if (maxBid > agent.getBalance()) {
                    System.out.println("Insufficient funds. Balance: $"
                            + agent.getBalance());
                    return;
                }
                System.out.println("Setting auto-bid maximum to $" + maxBid
                        + " for " + auctionAddress);
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a number.");
            }
        } catch (Exception e) {
            System.out.println("Error setting auto-bid: " + e.getMessage());
        }
    }
}