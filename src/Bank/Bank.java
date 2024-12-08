package Bank;

import constants.AuctionHouseAddress;
import constants.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.HashMap;

public class Bank extends Thread {
    public static HashMap<Integer, Account> accountHashMap = new HashMap<>();
    public static HashMap<AuctionInfo, AuctionHouseAddress> auctionHouseAddressHashMap =
            new HashMap<>();
    private static final boolean LOCAL = false;
    private ServerSocket bankSocket;

    public static void main(String[] args)
        throws IOException, ClassNotFoundException {
        Bank bank = new Bank();
        if(args.length != 1 && !LOCAL) {
            System.out.println("Wrong Arguments \n" +
                    "Usage: java -jar Bank.jar PORT_NUM_OF_BANK");
            System.exit(0);
        }
        if(LOCAL) {
            bank.start(8080);
        } else {
            bank.start(Integer.parseInt(args[0]));
        }
    }

    public void start(int port) throws IOException, ClassNotFoundException {
        Socket client;
        Message message;
        bankSocket = new ServerSocket(port);
        System.out.println("Bank is running on port " + port);
        System.out.println("Ready to accept new clients...");

        while(true) {
            try {
                System.out.println("Ready to accept new clients...");
                client = bankSocket.accept();
                System.out.println("New client connected from: " + client.getInetAddress());
                message = (Message) (new ObjectInputStream(client.getInputStream())).readObject();
                if(message.getCommand().equals("NewAgent")) {
                    new Thread(new AgentHandler(client)).start();
                    System.out.println("Connected to agent");
                } else if(message.getCommand().equals("NewAuctionHouse")) {
                    new Thread(new AuctionHandler(client)).start();
                    System.out.println("Connected to auction-house");
                }
            } catch (UnknownHostException u) {
                System.out.println("Invalid client port or IP sent to bank");
                break;
            }
        }
    }
}
