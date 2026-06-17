package AuctionHouse;

import constants.Message;
import constants.AuctionHouseAddress;
import Bank.AuctionInfo;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Represents an auction house server. Registers itself with the bank,
 * generates items to auction, and accepts agent connections to receive bids.
 */
public class AuctionHouse {
    private ServerSocket serverSocket;
    private List<Item> items;
    private Map<String, Auction> activeAuctions;
    private Socket bankSocket;
    private int accountNum;
    private ObjectOutputStream bankOut;
    private ObjectInputStream bankIn;

    public AuctionHouse(String bankHost, int bankPort) throws IOException, ClassNotFoundException {
        this.serverSocket = new ServerSocket(0);
        System.out.println("Server socket created on port: " + serverSocket.getLocalPort());

        this.items = generateItems();
        this.activeAuctions = new HashMap<>();
        connectToBank(bankHost, bankPort);
        initializeAuctions();
    }

    private List<Item> generateItems() {
        List<Item> items = new ArrayList<>();
        items.add(new Item("Old Bicycle", 1000.0, "Used for the England citizen"));
        items.add(new Item("Exclusive Printer", 500.0, "1980s Printer"));
        items.add(new Item("Old Car", 2000.0, "Volkswagen 1999"));
        for (Item item : items) {
            System.out.println("- " + item.getName() + " (Starting price: $"
                    + item.getStartingPrice() + ")");
        }
        return items;
    }

    private void connectToBank(String bankHost, int bankPort)
            throws IOException, ClassNotFoundException {
        this.bankSocket = new Socket(bankHost, bankPort);
        this.bankOut = new ObjectOutputStream(bankSocket.getOutputStream());
        bankOut.flush();
        this.bankIn = new ObjectInputStream(bankSocket.getInputStream());

        bankOut.writeObject(new Message("NewAuctionHouse"));
        bankOut.flush();

        Message response = (Message) bankIn.readObject();
        if (!"Ready".equals(response.getCommand())) {
            throw new IOException("Unexpected response: " + response.getCommand());
        }

        bankOut.writeObject(new Message("Create New Account"));
        bankOut.flush();

        response = (Message) bankIn.readObject();
        if ("AccountCreated".equals(response.getCommand())) {
            this.accountNum = (Integer) response.splitCommand(1);
            System.out.println("Account created with number: " + this.accountNum);
        }

        String localAddress = InetAddress.getLocalHost().getHostAddress();
        Random rand = new Random();
        String auctionID = "AuctionHouse" + rand.nextInt(10000);

        AuctionInfo auctionInfo = new AuctionInfo(accountNum, auctionID);
        auctionInfo.setItems(items);

        bankOut.writeObject(new Message("Auction Address", localAddress,
                serverSocket.getLocalPort(), auctionInfo));
        bankOut.flush();

        System.out.println("Registered with bank at " + localAddress
                + ":" + serverSocket.getLocalPort());
    }

    private void initializeAuctions() {
        for (Item item : items) {
            Auction auction = new Auction(item);
            activeAuctions.put(auction.getAuctionId(), auction);
            System.out.println("Created auction for: " + item.getName()
                    + " (ID: " + auction.getAuctionId() + ")");
        }
    }

    public Auction getAuction(String auctionId) { return activeAuctions.get(auctionId); }
    public Map<String, Auction> getActiveAuctions() { return new HashMap<>(activeAuctions); }
    public int getAccountNum() { return accountNum; }
    public List<Item> getItems() { return new ArrayList<>(items); }

    /**
     * Finds the auction ID whose item matches the given name.
     * Used to resolve bids that reference an item by name rather than
     * by internal auction ID, since agents only see item names.
     */
    public String findAuctionIdByItemName(String itemName) {
        for (Map.Entry<String, Auction> entry : activeAuctions.entrySet()) {
            if (entry.getValue().getItem().getName().equalsIgnoreCase(itemName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public synchronized boolean placeBid(String auctionId, int accountNum, double amount) {
        Auction auction = getAuction(auctionId);
        if (auction != null && auction.isActive()) {
            boolean success = auction.placeBid(accountNum, amount);
            if (success) {
                notifyBankOfBid(accountNum, amount);
            }
            return success;
        }
        return false;
    }

    private void notifyBankOfBid(int accountNum, double amount) {
        try {
            bankOut.writeObject(new Message("Block Funds", accountNum, (int) amount));
            bankOut.flush();
        } catch (IOException e) {
            System.err.println("Error notifying bank of bid: " + e.getMessage());
        }
    }

    public void start() {
        System.out.println("\nAuctionHouse running on port " + serverSocket.getLocalPort());
        for (Item item : items) {
            System.out.println("- " + item.getName() + " ($" + item.getStartingPrice() + ")");
        }

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New agent connected from: " + clientSocket.getInetAddress());
                new AgentHandler(clientSocket, this).start();
            } catch (IOException e) {
                System.err.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java AuctionHouse <bank-host> <bank-port>");
            System.exit(1);
        }
        try {
            AuctionHouse auctionHouse = new AuctionHouse(args[0], Integer.parseInt(args[1]));
            auctionHouse.start();
        } catch (Exception e) {
            System.err.println("Failed to start AuctionHouse: " + e.getMessage());
            System.exit(1);
        }
    }
}