package Agent;

import constants.Message;
import constants.AuctionHouseAddress;
import constants.StatusBid;
import Bank.AuctionInfo;
import java.io.*;
import java.net.*;
import java.util.*;

public abstract class Agent extends Thread {
    protected int accountNumber;
    protected String name;
    protected double initialBalance;
    protected Map<String, Socket> auctionSockets;
    protected Map<String, ObjectOutputStream> auctionOutputs;
    protected Map<String, ObjectInputStream> auctionInputs;
    protected Socket bankSocket;
    protected ObjectOutputStream bankOut;
    protected ObjectInputStream bankIn;

    public Agent(String name, double initialBalance) {
        this.name = name;
        this.initialBalance = initialBalance;
        this.auctionSockets = new HashMap<>();
        this.auctionOutputs = new HashMap<>();
        this.auctionInputs = new HashMap<>();
    }

    protected void connectToBank(String host, int port) throws IOException {
        bankSocket = new Socket(host, port);
        bankOut = new ObjectOutputStream(bankSocket.getOutputStream());
        bankIn = new ObjectInputStream(bankSocket.getInputStream());

        // Register with bank
        bankOut.writeObject(new Message("NewAgent"));

        // Create account
        bankOut.writeObject(new Message("CreateNewAccount", initialBalance, name));
        try {
            Message response = (Message) bankIn.readObject();
            accountNumber = (Integer) response.splitCommand(1);
            System.out.println("Bank account created: " + accountNumber);
        } catch (ClassNotFoundException e) {
            throw new IOException("Error creating bank account", e);
        }
    }

    protected void updateAuctionHouses() throws IOException, ClassNotFoundException {
        bankOut.writeObject(new Message("ViewCurrentAuctions"));
        Message response = (Message) bankIn.readObject();
        @SuppressWarnings("unchecked")
        HashMap<AuctionInfo, AuctionHouseAddress> auctions =
                (HashMap<AuctionInfo, AuctionHouseAddress>) response.splitCommand(1);

        for (AuctionHouseAddress address : auctions.values()) {
            connectToAuctionHouse(address);
        }
    }

    protected void connectToAuctionHouse(AuctionHouseAddress address) throws IOException {
        String key = address.getIpAddress() + ":" + address.getPortNum();
        if (!auctionSockets.containsKey(key)) {
            Socket socket = new Socket(address.getIpAddress(), address.getPortNum());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            auctionSockets.put(key, socket);
            auctionOutputs.put(key, out);
            auctionInputs.put(key, in);

            // Register with auction house
            out.writeObject(new Message("Register", accountNumber));

            // Start listener for this auction house
            startAuctionListener(key);
        }
    }

    private void startAuctionListener(String auctionKey) {
        new Thread(() -> {
            try {
                ObjectInputStream in = auctionInputs.get(auctionKey);
                while (true) {
                    Message message = (Message) in.readObject();
                    handleAuctionMessage(message, auctionKey);
                }
            } catch (Exception e) {
                System.out.println("Auction connection closed: " + auctionKey);
                disconnectFromAuction(auctionKey);
            }
        }).start();
    }

    protected void disconnectFromAuction(String auctionKey) {
        try {
            if (auctionSockets.containsKey(auctionKey)) {
                auctionSockets.get(auctionKey).close();
                auctionSockets.remove(auctionKey);
                auctionOutputs.remove(auctionKey);
                auctionInputs.remove(auctionKey);
            }
        } catch (IOException e) {
            System.out.println("Error disconnecting from auction: " + e.getMessage());
        }
    }

    protected void placeBid(String auctionKey, String itemId, double amount) throws IOException {
        ObjectOutputStream out = auctionOutputs.get(auctionKey);
        if (out != null) {
            out.writeObject(new Message("PlaceBid", itemId, amount));
        }
    }

    protected double getBalance() throws IOException, ClassNotFoundException {
        bankOut.writeObject(new Message("availableBalance"));
        Message response = (Message) bankIn.readObject();
        return (Double) response.splitCommand(1);
    }

    protected void cleanup() {
        try {
            // Notify bank of termination
            bankOut.writeObject(new Message("Terminates"));
            bankSocket.close();

            // Close all auction connections
            for (String key : auctionSockets.keySet()) {
                disconnectFromAuction(key);
            }
        } catch (IOException e) {
            System.out.println("Error during cleanup: " + e.getMessage());
        }
    }

    protected abstract void handleAuctionMessage(Message message, String auctionKey);
}
