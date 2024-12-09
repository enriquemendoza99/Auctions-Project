package AuctionHouse;

import constants.Message;
import java.io.*;
import java.net.Socket;

public class AgentHandler extends Thread {
    private Socket socket;
    private AuctionHouse auctionHouse;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public AgentHandler(Socket socket, AuctionHouse auctionHouse) {
        this.socket = socket;
        this.auctionHouse = auctionHouse;
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            this.in = new ObjectInputStream(socket.getInputStream());
            System.out.println("AgentHandler: Streams initialized");
        } catch (IOException e) {
            System.err.println("Error creating streams: " + e.getMessage());
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
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void processMessage(Message message) throws IOException {
        try {
            switch (message.getCommand()) {
                case "GetAuctions":
                    sendAuctionList();
                    break;
                case "PlaceBid":
                    handleBid(message);
                    break;
                case "GetItems":
                    sendItemList();
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            sendError(e.getMessage());
        }
    }

    private void sendAuctionList() throws IOException {
        out.writeObject(new Message("AuctionList", auctionHouse.getActiveAuctions()));
        out.flush();
    }

    private void sendItemList() throws IOException {
        out.writeObject(new Message("ItemList", auctionHouse.getItems()));
        out.flush();
    }

    private void handleBid(Message message) throws IOException {
        String auctionId = (String) message.splitCommand(1);
        int accountNum = (Integer) message.splitCommand(2);
        double amount = (Double) message.splitCommand(3);

        boolean success = auctionHouse.placeBid(auctionId, accountNum, amount);
        if (success) {
            out.writeObject(new Message("BidAccepted", auctionId, amount));
        } else {
            out.writeObject(new Message("BidRejected", auctionId));
        }
        out.flush();
    }

    private void sendError(String error) throws IOException {
        out.writeObject(new Message("Error", error));
        out.flush();
    }
}
