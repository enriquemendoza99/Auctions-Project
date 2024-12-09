package AuctionHouse;

import constants.Message;
import java.io.*;
import java.net.*;
import java.util.*;
import constants.AuctionHouseAddress;

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

    private void connectToBank(String bankHost, int bankPort) throws IOException, ClassNotFoundException {
        try {
            this.bankSocket = new Socket(bankHost, bankPort);
            System.out.println("Connected to bank successfully");

            // Create streams
            this.bankOut = new ObjectOutputStream(bankSocket.getOutputStream());
            bankOut.flush();
            this.bankIn = new ObjectInputStream(bankSocket.getInputStream());

            // Send initial identification
            System.out.println("Sending NewAuctionHouse message to bank...");
            bankOut.writeObject(new Message("NewAuctionHouse"));
            bankOut.flush();

            // Wait for ready message
            Message response = (Message) bankIn.readObject();
            if (!"Ready".equals(response.getCommand())) {
                throw new IOException("Unexpected response: " + response.getCommand());
            }

            // Create account
            System.out.println("Requesting new bank account...");
            bankOut.writeObject(new Message("Create New Account"));
            bankOut.flush();

            response = (Message) bankIn.readObject();
            if ("AccountCreated".equals(response.getCommand())) {
                this.accountNum = (Integer) response.splitCommand(1);
                System.out.println("Account created with number: " + this.accountNum);
            } else {
                throw new IOException("Failed to create account");
            }

            // Register address
            String localAddress = InetAddress.getLocalHost().getHostAddress();
            bankOut.writeObject(new Message("Auction Address", localAddress, serverSocket.getLocalPort()));
            bankOut.flush();

            response = (Message) bankIn.readObject();
            if (!"AddressRegistered".equals(response.getCommand())) {
                throw new IOException("Failed to register address");
            }
            System.out.println("Address registered with bank");

        } catch (IOException e) {
            System.err.println("Error connecting to bank: " + e.getMessage());
            throw e;
        }
    }

    private List<Item> generateItems() {
        List<Item> items = new ArrayList<>();
        items.add(new Item("Antique Vase", 1000.0, "A beautiful Ming dynasty vase"));
        items.add(new Item("Vintage Watch", 500.0, "1950s Rolex Submariner"));
        items.add(new Item("Oil Painting", 2000.0, "19th century landscape"));
        System.out.println("Generated " + items.size() + " items for auction");
        return items;
    }

    private void initializeAuctions() {
        for (Item item : items) {
            Auction auction = new Auction(item);
            activeAuctions.put(auction.getAuctionId(), auction);
            System.out.println("Created auction for item: " + item.getName() +
                    " with starting price: $" + item.getStartingPrice());
        }
    }

    public Auction getAuction(String auctionId) {
        return activeAuctions.get(auctionId);
    }

    public List<Item> getAvailableItems() {
        List<Item> availableItems = new ArrayList<>();
        for (Auction auction : activeAuctions.values()) {
            if (auction.isActive()) {
                availableItems.add(auction.getItem());
            }
        }
        return availableItems;
    }

    public void start() {
        System.out.println("\nAuctionHouse is now running on port " + serverSocket.getLocalPort());
        System.out.println("Waiting for agents to connect...\n");

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
            String bankHost = args[0];
            int bankPort = Integer.parseInt(args[1]);

            System.out.println("Starting AuctionHouse with following parameters:");
            System.out.println("Bank Host: " + bankHost);
            System.out.println("Bank Port: " + bankPort);

            AuctionHouse auctionHouse = new AuctionHouse(bankHost, bankPort);
            auctionHouse.start();
        } catch (IOException e) {
            System.err.println("Failed to start AuctionHouse: " + e.getMessage());
            System.err.println("Please ensure the bank is running at the specified host and port");
            System.exit(1);
        } catch (ClassNotFoundException e) {
            System.err.println("Error with message serialization: " + e.getMessage());
            System.exit(1);
        } catch (NumberFormatException e) {
            System.err.println("Error: Port must be a number");
            System.exit(1);
        }
    }
}

