package Agent;

import constants.AuctionHouseAddress;
import java.util.Scanner;

public class UserInterface extends Thread {
    private Agent agent;
    private Scanner scanner;
    private boolean running;

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
                case "1":
                    System.out.println("Current Balance: $" + agent.getBalance());
                    break;
                case "2":
                    System.out.println("Fetching available auctions...");
                    // Implementation for viewing auctions
                    break;
                case "3":
                    placeBid();
                    break;
                case "4":
                    setAutoBid();
                    break;
                case "5":
                    System.out.println("Exiting...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error processing choice: " + e.getMessage());
        }
    }

    private void placeBid() {
        System.out.print("Enter auction ID: ");
        String auctionId = scanner.nextLine();
        System.out.print("Enter bid amount: $");
        try {
            double amount = Double.parseDouble(scanner.nextLine());
            agent.placeBid(auctionId, amount);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a number.");
        }
    }

    private void setAutoBid() {
        System.out.print("Enter auction ID: ");
        String auctionId = scanner.nextLine();
        System.out.print("Enter maximum bid amount: $");
        try {
            double maxBid = Double.parseDouble(scanner.nextLine());
            // Implementation for setting auto-bid
            System.out.println("Auto-bid set successfully");
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a number.");
        }
    }
}