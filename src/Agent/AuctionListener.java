package Agent;

import constants.Message;
import java.io.*;
import java.net.*;

public class AuctionListener implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private boolean running;

    public AuctionListener(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new ObjectInputStream(socket.getInputStream());
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Message message = (Message) in.readObject();
                processAuctionMessage(message);
            } catch (Exception e) {
                if (running) {
                    System.out.println("Error in auction listener:");
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void processAuctionMessage(Message message) {
        System.out.println("\nReceived from auction house: " + message.getCommand());

        switch (message.getCommand()) {
            case "ItemList":
                displayItems(message);
                break;
            case "acceptance":
                System.out.println("Bid accepted!");
                break;
            case "rejection":
                System.out.println("Bid rejected!");
                break;
            case "outbid":
                System.out.println("You have been outbid!");
                break;
            case "winner":
                System.out.println("Congratulations! You won the auction for item: " + message.splitCommand(1));
                break;
            default:
                System.out.println("Unknown message: " + message.getCommand());
        }
    }

    private void displayItems(Message message) {
        try {
            java.util.List<?> items = (java.util.List<?>) message.splitCommand(1);
            System.out.println("\nAvailable Items:");
            for (Object item : items) {
                System.out.println(item.toString());
            }
        } catch (Exception e) {
            System.out.println("Error displaying items: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
    }
}
