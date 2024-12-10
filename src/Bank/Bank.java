package Bank;

import constants.AuctionHouseAddress;
import constants.Message;
import java.io.*;
import java.net.*;
import java.util.HashMap;

public class Bank {
    public static HashMap<Integer, Account> accountHashMap = new HashMap<>();
    public static HashMap<AuctionInfo, AuctionHouseAddress>
            auctionHouseAddressHashMap = new HashMap<>();
    private ServerSocket bankSocket;

    public void start(int port) {
        try {
            bankSocket = new ServerSocket(port);
            System.out.println("Bank server started on port " + port);
            System.out.println("Waiting for connections...");

            while(true) {
                try {
                    // Accept new connection
                    Socket client = bankSocket.accept();
                    System.out.println("\nNew client connected from: " +
                            client.getInetAddress());
                    // Create streams
                    ObjectOutputStream out = new
                            ObjectOutputStream(client.getOutputStream());
                    out.flush();
                    ObjectInputStream in = new
                            ObjectInputStream(client.getInputStream());
                    System.out.println("Streams established");
                    // Read initial message
                    Message message = (Message) in.readObject();
                    System.out.println("Received initial message: " +
                            message.getCommand());

                    switch(message.getCommand()) {
                        case "NewAgent":
                            System.out.println("Starting new agent handler");
                            AgentHandler agentHandler = new
                                    AgentHandler(client, in, out);
                            agentHandler.start();
                            System.out.println("Agent handler started");
                            break;
                        case "NewAuctionHouse":
                            System.out.println("Starting new auction " +
                                    "house handler");
                            AuctionHandler auctionHandler = new
                                    AuctionHandler(client, in, out);
                            auctionHandler.start();
                            System.out.println("Auction handler started");
                            break;
                        default:
                            System.out.println("Unknown agent");
                            out.writeObject(new Message("Error",
                                    "Unknown client type"));
                            out.flush();
                            client.close();
                    }
                } catch (Exception e) {
                    System.err.println("Error handling client: " +
                            e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + port);
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Missing argument need to follow this pattern " +
                    "java Bank <port-number>");
            System.exit(1);
        }
        Bank bank = new Bank();
        bank.start(Integer.parseInt(args[0]));
    }
}