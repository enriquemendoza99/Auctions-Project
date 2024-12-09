package AuctionHouse;

import constants.Message;
import java.io.*;
import java.net.Socket;

public class AgentHandler extends Thread {
    private Socket agentSocket;
    private AuctionHouse auctionHouse;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int agentAccountNum;

    public AgentHandler(Socket agentSocket, AuctionHouse auctionHouse) {
        this.agentSocket = agentSocket;
        this.auctionHouse = auctionHouse;
        try {
            this.out = new ObjectOutputStream(agentSocket.getOutputStream());
            this.in = new ObjectInputStream(agentSocket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error creating agent handler: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message message = (Message) in.readObject();
                processMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in agent handler: " + e.getMessage());
        } finally {
            try {
                agentSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing agent socket: " + e.getMessage());
            }
        }
    }

    private void processMessage(Message message) throws IOException {
        switch (message.getCommand()) {
            case "RegisterAgent":
                agentAccountNum = (Integer) message.splitCommand(1);
                break;
            case "PlaceBid":
                handleBid(message);
                break;
            case "RequestItems":
                sendAvailableItems();
                break;
        }
    }

    private void handleBid(Message message) throws IOException {
        int accountNum = (Integer) message.splitCommand(1);
        double amount = (Double) message.splitCommand(2);
        String auctionId = (String) message.splitCommand(3);

        Auction auction = auctionHouse.getAuction(auctionId);
        if (auction != null && auction.placeBid(accountNum, amount)) {
            out.writeObject(new Message("BidAccepted", auctionId, amount));
        } else {
            out.writeObject(new Message("BidRejected", auctionId));
        }
    }

    private void sendAvailableItems() throws IOException {
        out.writeObject(new Message("ItemList", auctionHouse.getAvailableItems()));
    }
}