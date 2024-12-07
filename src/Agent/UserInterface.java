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
            System.out.println("\n1. View Balance");
            System.out.println("2. View Auction Houses");
            System.out.println("3. View Items");
            System.out.println("4. Place Bid");
            System.out.println("5. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            try {
                handleMenuChoice(choice);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMenuChoice(int choice) throws IOException, ClassNotFoundException {
        switch (choice) {
            case 1:
                checkBalance();
                break;
            case 2:
                viewAuctionHouses();
                break;
            case 3:
                agent.getAuctionManager().viewItems();
                break;
            case 4:
                placeBid();
                break;
            case 5:
                exit();
                break;
        }
    }

    private void checkBalance() throws IOException, ClassNotFoundException {
        agent.getBankConnection().sendMessage(new Message("availableBalance"));
        Message response = agent.getBankConnection().receiveMessage();
        System.out.println("Available Balance: $" + response.splitCommand(1));
    }

    private void viewAuctionHouses() throws IOException, ClassNotFoundException {
        agent.getBankConnection().sendMessage(new Message("ViewCurrentAuctions"));
        Message response = agent.getBankConnection().receiveMessage();
        HashMap<?, AuctionHouseAddress> auctions =
                (HashMap<?, AuctionHouseAddress>) response.splitCommand(1);

        for (Map.Entry<?, AuctionHouseAddress> entry : auctions.entrySet()) {
            agent.getAuctionManager().connectToAuctionHouse(entry.getValue());
        }
    }

    private void placeBid() throws IOException {
        System.out.println("Enter auction house address:");
        String auctionAddress = scanner.nextLine();
        System.out.println("Enter item ID:");
        String itemId = scanner.nextLine();
        System.out.println("Enter bid amount:");
        int bidAmount = scanner.nextInt();

        agent.getAuctionManager().placeBid(auctionAddress, itemId,
                agent.getAccountNum(), bidAmount);
    }

    private void exit() throws IOException {
        agent.setRunning(false);
        agent.getBankConnection().close();
        agent.getAuctionManager().closeAll();
        System.exit(0);
    }
}