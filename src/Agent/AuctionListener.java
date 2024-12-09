package Agent;

import constants.Message;
import java.io.*;
import java.net.Socket;

public class AuctionListener extends Thread {
    private Socket auctionSocket;
    private Agent agent;
    private ObjectInputStream in;
    private boolean running;

    public AuctionListener(Socket auctionSocket, Agent agent) {
        this.auctionSocket = auctionSocket;
        this.agent = agent;
        this.running = true;
        try {
            this.in = new ObjectInputStream(auctionSocket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error creating auction listener: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Message message = (Message) in.readObject();
                processMessage(message);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error in auction listener: " + e.getMessage());
                running = false;
            }
        }
    }

    private void processMessage(Message message) {
        switch (message.getCommand()) {
            case "BidAccepted":
                System.out.println("Bid accepted for item: " + message.splitCommand(1));
                break;
            case "BidRejected":
                System.out.println("Bid rejected: " + message.splitCommand(1));
                break;
            case "AuctionWon":
                System.out.println("Won auction for item: " + message.splitCommand(1));
                break;
            case "OutBid":
                System.out.println("Outbid on item: " + message.splitCommand(1));
                break;
        }
    }

    public void stopListening() {
        running = false;
    }
}