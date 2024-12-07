package Agent;

import constants.AuctionHouseAddress;
import constants.Message;
import java.util.*;
import java.io.IOException;

public class UserInterface {
    private Agent agent;
    private Scanner scanner;

    public UserInterface(Agent agent) {
        this.agent = agent;
        this.scanner = new Scanner(System.in);
    }

    public void showMenu() {
        while (agent.isRunning()) {
            System.out.println("\n=== Agent Menu ===");
            System.out.println("1. View Balance");
            System.out.println("2. View Auction Houses");
            System.out.println("3. View Items");
            System.out.println("4. Place Bid");
            System.out.println("5. Exit");
            System.out.print("\nEnter choice (1-5): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            try {
                handleMenuChoice(choice);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error in menu: " + e.getMessage());
            }
        }
    }

    private void handleMenuChoice(int choice) throws IOException, ClassNotFoundException {
        switch (choice) {
            case 1:
                agent.checkBalance();
                break;
            case 2:
                agent.viewAuctionHouses();
                break;
            case 3:
                agent.getAuctionManager().viewItems();
                break;
            case 4:
                agent.placeBid();
                break;
            case 5:
                agent.exit();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    public void placeBid() throws IOException {
        if (agent.getAuctionManager().getAuctionAddresses().isEmpty()) {
            System.out.println("No auction houses connected. View auction houses first.");
            return;
        }

        System.out.println("\nAvailable auction houses:");
        for (String address : agent.getAuctionManager().getAuctionAddresses()) {
            System.out.println(address);
        }

        System.out.print("Enter auction house address (IP:PORT): ");
        String auctionAddress = scanner.nextLine();
        System.out.print("Enter item ID: ");
        String itemId = scanner.nextLine();
        System.out.print("Enter bid amount: $");
        int bidAmount = scanner.nextInt();

        agent.getAuctionManager().placeBid(auctionAddress, itemId, agent.getAccountNum(), bidAmount);
    }
}