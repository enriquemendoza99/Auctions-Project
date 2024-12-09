package Agent;

import constants.AuctionHouseAddress;
import constants.Message;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class AuctionManager {
    private Agent agent;
    private Map<String, Socket> auctionConnections;
    private Map<String, ObjectOutputStream> outputStreams;
    private Map<String, AuctionListener> listeners;

    public AuctionManager(Agent agent) {
        this.agent = agent;
        this.auctionConnections = new HashMap<>();
        this.outputStreams = new HashMap<>();
        this.listeners = new HashMap<>();
    }

    public void connectToAuctionHouse(AuctionHouseAddress address) {
        try {
            Socket socket = new Socket(address.getIpAddress(), address.getPortNum());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            // Send initial registration message
            out.writeObject(new Message("RegisterAgent", agent.getAccountNum()));

            // Create and start listener
            AuctionListener listener = new AuctionListener(socket, agent);
            listener.start();

            // Store connections
            String auctionId = address.getIpAddress() + ":" + address.getPortNum();
            auctionConnections.put(auctionId, socket);
            outputStreams.put(auctionId, out);
            listeners.put(auctionId, listener);

        } catch (IOException e) {
            System.err.println("Failed to connect to auction house: " + e.getMessage());
        }
    }

    public void placeBid(String auctionId, double amount) {
        ObjectOutputStream out = outputStreams.get(auctionId);
        if (out != null) {
            try {
                out.writeObject(new Message("PlaceBid", agent.getAccountNum(), amount));
            } catch (IOException e) {
                System.err.println("Failed to place bid: " + e.getMessage());
            }
        }
    }

    public void closeConnections() {
        for (Map.Entry<String, Socket> entry : auctionConnections.entrySet()) {
            try {
                listeners.get(entry.getKey()).stopListening();
                entry.getValue().close();
            } catch (IOException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
