package Agent;

import constants.Message;
import constants.AuctionHouseAddress;
import Bank.AuctionInfo;
import java.io.*;
import java.net.*;
import java.util.*;

public class Agent {
    private BankConnection bankConnection;
    private AuctionManager auctionManager;
    private int accountNum;
    private UserInterface userInterface;
    private boolean isAuto;
    private boolean running;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java Agent.Agent BANK_PORT NAME INITIAL_BALANCE [auto]");
            System.exit(0);
        }

        try {
            System.out.println("Starting Agent...");
            boolean isAuto = args.length > 3 && args[3].equals("auto");
            Agent agent = new Agent(isAuto);
            System.out.println("Connecting to bank on port " + args[0]);
            agent.start(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]));
            agent.showMenu(); // Call the showMenu() method after the agent has started
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error starting Agent:");
            e.printStackTrace();
        }
    }

    public Agent(boolean isAuto) {
        System.out.println("Initializing Agent...");
        this.isAuto = isAuto;
        this.userInterface = new UserInterface(this);
        this.running = true;
        this.auctionManager = new AuctionManager();
    }

    public void start(int bankPort, String name, int initialBalance) throws IOException, ClassNotFoundException {
        try {
            System.out.println("Connecting to bank at localhost:" + bankPort);
            bankConnection = new BankConnection(bankPort);
            System.out.println("Connected to bank");

            System.out.println("Registering with bank...");
            bankConnection.sendMessage(new Message("NewAgent"));
            bankConnection.sendMessage(new Message("CreateNewAccount", initialBalance, name));

            Message response = bankConnection.receiveMessage();
            accountNum = (Integer) response.splitCommand(1);
            System.out.println("Account created with number: " + accountNum);

            if (isAuto) {
                runAutoBidder();
            } else {
                showMenu();
            }
        } catch (Exception e) {
            System.out.println("Error during startup:");
            e.printStackTrace();
            throw e;
        }
    }

    private void showMenu() {
        this.userInterface.showMenu();
    }

    public void checkBalance() throws IOException, ClassNotFoundException {
        System.out.println("Checking balance...");
        bankConnection.sendMessage(new Message("availableBalance"));
        Message response = bankConnection.receiveMessage();
        System.out.println("Available Balance: $" + response.splitCommand(1));
    }

    public void viewAuctionHouses() throws IOException, ClassNotFoundException {
        System.out.println("Requesting auction house list...");
        bankConnection.sendMessage(new Message("ViewCurrentAuctions"));
        Message response = bankConnection.receiveMessage();
        @SuppressWarnings("unchecked")
        HashMap<AuctionInfo, AuctionHouseAddress> auctions =
                (HashMap<AuctionInfo, AuctionHouseAddress>) response.splitCommand(1);

        if (auctions.isEmpty()) {
            System.out.println("No auction houses available.");
            return;
        }

        System.out.println("\nAvailable Auction Houses:");
        for (Map.Entry<AuctionInfo, AuctionHouseAddress> entry : auctions.entrySet()) {
            AuctionHouseAddress addr = entry.getValue();
            String addressKey = addr.getIpAddress() + ":" + addr.getPortNum();
            System.out.println(addressKey);
            auctionManager.connectToAuctionHouse(addr);
        }
    }

    public void placeBid() throws IOException {
        this.userInterface.placeBid();
    }

    public void exit() throws IOException {
        System.out.println("Exiting...");
        running = false;

        bankConnection.sendMessage(new Message("Terminates"));
        bankConnection.close();
        auctionManager.closeAll();

        System.out.println("Goodbye!");
        System.exit(0);
    }

    private void runAutoBidder() {
        System.out.println("Starting auto-bidder mode...");
        Random random = new Random();

        while (running) {
            try {
                Thread.sleep(5000);
                viewAuctionHouses();
                auctionManager.viewItems();
                // Auto-bidding logic could be implemented here
            } catch (Exception e) {
                if (running) {
                    System.out.println("Error in auto-bidder:");
                    e.printStackTrace();
                }
            }
        }
    }

    // Getters for other classes to use
    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getAccountNum() {
        return accountNum;
    }

    public BankConnection getBankConnection() {
        return bankConnection;
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }
}