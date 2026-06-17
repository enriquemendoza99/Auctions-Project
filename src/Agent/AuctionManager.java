package Agent;

import constants.AuctionHouseAddress;
import constants.Message;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages this agent's connections to one or more auction houses.
 * Each auction house connection is keyed by its "ip:port" address.
 * An agent must connect to an auction house before it can place bids there.
 */
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

    /**
     * Connects to an auction house if not already connected, and registers
     * this agent's account number with it.
     * @param address the auction house's network address
     * @return the auctionId key used to reference this connection
     */
    public String connectToAuctionHouse(AuctionHouseAddress address) {
        String auctionId = address.getIpAddress() + ":" + address.getPortNum();
        if (auctionConnections.containsKey(auctionId)) {
            return auctionId; // already connected
        }
        try {
            Socket socket = new Socket(address.getIpAddress(), address.getPortNum());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            // Register this agent with the auction house
            out.writeObject(new Message("RegisterAgent", agent.getAccountNum()));
            out.flush();

            AuctionListener listener = new AuctionListener(socket, agent);
            listener.start();

            auctionConnections.put(auctionId, socket);
            outputStreams.put(auctionId, out);
            listeners.put(auctionId, listener);

            System.out.println("Connected to auction house at " + auctionId);
        } catch (IOException e) {
            System.err.println("Failed to connect to auction house: " + e.getMessage());
        }
        return auctionId;
    }

    /**
     * Places a bid on a specific item at the given auction house.
     * Connects to the auction house first if not already connected.
     * @param address the auction house's network address
     * @param itemName the name of the item being bid on
     * @param amount the bid amount
     */
    public void placeBid(AuctionHouseAddress address, String itemName, double amount) {
        String auctionId = connectToAuctionHouse(address);
        ObjectOutputStream out = outputStreams.get(auctionId);
        if (out != null) {
            try {
                // Send command, item name, account number, and amount —
                // matching exactly what AuctionHouse.AgentHandler expects
                out.writeObject(new Message("PlaceBid", itemName,
                        agent.getAccountNum(), amount));
                out.flush();
            } catch (IOException e) {
                System.err.println("Failed to place bid: " + e.getMessage());
            }
        } else {
            System.err.println("No connection available for auction: " + auctionId);
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
