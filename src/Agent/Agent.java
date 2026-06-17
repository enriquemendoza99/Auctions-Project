package Agent;

import constants.AuctionHouseAddress;
import java.io.IOException;
import java.util.HashMap;

/**
 * Represents a bidding agent that connects to a bank to manage funds and
 * to one or more auction houses to browse and bid on items.
 */
public class Agent {
    private int accountNum;
    private BankConnection bankConnection;
    private AuctionManager auctionManager;
    private UserInterface userInterface;
    private AutoBidder autoBidder;
    private double balance;

    public Agent(String bankHost, int bankPort, String agentName, int initialFunds) {
        try {
            System.out.println("Connecting to bank at " + bankHost + ":" + bankPort);
            this.bankConnection = new BankConnection(bankHost, bankPort);

            this.accountNum = bankConnection.createAccount(agentName, initialFunds);
            System.out.println("Created account with number: " + accountNum);

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

    public int getAccountNum() { return accountNum; }
    public double getBalance() { return balance; }
    public void updateBalance(double newBalance) { this.balance = newBalance; }

    /**
     * Places a bid on an item at a specific auction house.
     * Delegates to the AuctionManager, which handles connecting
     * to the auction house if needed.
     */
    public void placeBid(AuctionHouseAddress address, String itemName, double amount) {
        auctionManager.placeBid(address, itemName, amount);
    }

    public HashMap<Object, AuctionHouseAddress> getAvailableAuctions()
            throws IOException, ClassNotFoundException {
        return bankConnection.getAvailableAuctions();
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java Agent <bank-host> <bank-port> " +
                    "<agent-name> <initial-funds>");
            System.exit(1);
        }
        try {
            String bankHost = args[0];
            int bankPort = Integer.parseInt(args[1]);
            String agentName = args[2];
            int initialFunds = Integer.parseInt(args[3]);

            Agent agent = new Agent(bankHost, bankPort, agentName, initialFunds);
            agent.start();
        } catch (NumberFormatException e) {
            System.err.println("Error: Port and initial funds must be numbers");
        }
    }
}