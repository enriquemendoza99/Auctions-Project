package Agent;

import constants.Message;
import java.util.*;
import java.io.*;
import java.net.*;
import constants.AuctionHouseAddress;

public class AuctionManager {
    private HashMap<String, Socket> auctionSockets;
    private HashMap<Socket, ObjectOutputStream> outputStreams;

    public AuctionManager() {
        auctionSockets = new HashMap<>();
        outputStreams = new HashMap<>();
    }

    public void connectToAuctionHouse(AuctionHouseAddress address) throws IOException {
        String key = address.getIpAddress() + ":" + address.getPortNum();
        if (!auctionSockets.containsKey(key)) {
            Socket socket = new Socket(address.getIpAddress(), address.getPortNum());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            auctionSockets.put(key, socket);
            outputStreams.put(socket, out);
            System.out.println("Connected to auction house at " + key);
        }
    }

    public void placeBid(String auctionAddress, String itemId, int accountNum, int bidAmount) throws IOException {
        Socket socket = auctionSockets.get(auctionAddress);
        if (socket != null && socket.isConnected()) {
            ObjectOutputStream out = outputStreams.get(socket);
            if (out == null) {
                out = new ObjectOutputStream(socket.getOutputStream());
                outputStreams.put(socket, out);
            }
            out.writeObject(new Message("Bid", itemId, accountNum, bidAmount));
            System.out.println("Bid sent to auction house: $" + bidAmount);
        } else {
            System.out.println("Could not connect to auction house at " + auctionAddress);
        }
    }

    public void viewItems() throws IOException {
        if (auctionSockets.isEmpty()) {
            System.out.println("No auction houses connected.");
            return;
        }

        for (Map.Entry<String, Socket> entry : auctionSockets.entrySet()) {
            Socket socket = entry.getValue();
            if (socket.isConnected()) {
                ObjectOutputStream out = outputStreams.get(socket);
                if (out == null) {
                    out = new ObjectOutputStream(socket.getOutputStream());
                    outputStreams.put(socket, out);
                }
                out.writeObject(new Message("GetItems"));
                System.out.println("Requested items from " + entry.getKey());
            }
        }
    }

    public void closeAll() throws IOException {
        for (Socket socket : auctionSockets.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
        auctionSockets.clear();
        outputStreams.clear();
    }

    public Set<String> getAuctionAddresses() {
        return auctionSockets.keySet();
    }
}