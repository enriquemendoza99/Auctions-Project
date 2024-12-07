package AuctionHouse;

import constants.Message;
import java.io.*;
import java.net.Socket;

public class AgentHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private AuctionHouse auctionHouse;
    private boolean running;

    public AgentHandler(Socket socket, AuctionHouse auctionHouse) throws IOException {
        this.socket = socket;
        this.auctionHouse = auctionHouse;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Message message = (Message) in.readObject();
                processMessage(message);
            } catch (IOException | ClassNotFoundException e) {
                running = false;
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void sendMessage(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    private void processMessage(Message message) throws IOException {
        System.out.println("Received message from agent: " + message.getCommand());
        switch (message.getCommand()) {
            case "GetItems":
                sendMessage(new Message("ItemList", auctionHouse.getItems()));
                break;
            case "Bid":
                String itemId = (String) message.splitCommand(1);
                int agentAccount = (Integer) message.splitCommand(2);
                int bidAmount = (Integer) message.splitCommand(3);
                auctionHouse.processBid(itemId, agentAccount, bidAmount, this);
                break;
        }
    }
}

