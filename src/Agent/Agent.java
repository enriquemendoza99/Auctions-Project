package Agent;

import constants.Message;
import constants.AuctionHouseAddress;
import java.io.*;
import java.net.Socket;

public class Agent {
    private int accountNum;
    private BankConnection bankConnection;
    private AuctionManager auctionManager;
    private UserInterface userInterface;
    private AutoBidder autoBidder;
    private double balance;

    public Agent(String bankHost, int bankPort, String agentName, int initialFunds) {
        try {
            System.out.println("Attempting to connect to bank at " + bankHost + ":" + bankPort);

            // Connect to bank
            this.bankConnection = new BankConnection(bankHost, bankPort);
            System.out.println("Connected to bank successfully");

            // Create account and get account number
            this.accountNum = bankConnection.createAccount(agentName, initialFunds);
            System.out.println("Created account with number: " + accountNum);

            // Initialize components
            this.balance = initialFunds;
            this.auctionManager = new AuctionManager(this);
            this.userInterface = new UserInterface(this);
            this.autoBidder = new AutoBidder(this);

            System.out.println("Agent initialization completed successfully");

        } catch (Exception e) {
            System.err.println("Failed to initialize Agent: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void start() {
        System.out.println("Starting agent interface...");
        if (userInterface != null && autoBidder != null) {
            userInterface.start();
            autoBidder.start();
        } else {
            System.err.println("Agent not properly initialized. Cannot start.");
            System.exit(1);
        }
    }

    public int getAccountNum() {
        return accountNum;
    }

    public double getBalance() {
        return balance;
    }

    public void updateBalance(double newBalance) {
        this.balance = newBalance;
    }

    public void placeBid(String auctionId, double amount) {
        auctionManager.placeBid(auctionId, amount);
    }

    public void registerWithAuctionHouse(AuctionHouseAddress address) {
        auctionManager.connectToAuctionHouse(address);
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java Agent <bank-host> <bank-port> <agent-name> <initial-funds>");
            System.exit(1);
        }

        try {
            String bankHost = args[0];
            int bankPort = Integer.parseInt(args[1]);
            String agentName = args[2];
            int initialFunds = Integer.parseInt(args[3]);

            System.out.println("Starting agent with following parameters:");
            System.out.println("Bank Host: " + bankHost);
            System.out.println("Bank Port: " + bankPort);
            System.out.println("Agent Name: " + agentName);
            System.out.println("Initial Funds: " + initialFunds);

            Agent agent = new Agent(bankHost, bankPort, agentName, initialFunds);
            agent.start();
        } catch (NumberFormatException e) {
            System.err.println("Error: Port and initial funds must be numbers");
            System.exit(1);
        }
    }
}