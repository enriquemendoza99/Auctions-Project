package Bank;

import constants.AuctionHouseAddress;
import constants.Message;
import AuctionHouse.Item;
import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AuctionHandler extends Thread {
    private Socket socket;
    private Account account;
    private Account agentAccount;
    private AuctionHouseAddress auctionHouseAddress;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public AuctionHandler(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        System.out.println("AuctionHandler: Using existing streams");
        try {
            out.writeObject(new Message("Ready"));
            out.flush();
            System.out.println("AuctionHandler: Sent ready message");
        } catch (IOException e) {
            System.err.println("Error in AuctionHandler constructor: " + e.getMessage());
        }
    }

    private void processMessage(Message message) throws IOException {
        System.out.println("Processing auction message: " + message.getCommand());

        try {
            switch(message.getCommand()) {
                case "Create New Account":
                    this.account = new Account(0, Bank.accountHashMap);
                    Bank.accountHashMap.put(account.getAccountNum(), account);
                    out.writeObject(new Message("AccountCreated", account.getAccountNum()));
                    out.flush();
                    System.out.println("Created auction account: " + account.getAccountNum());
                    break;

                case "Auction Address":
                    String ipAddress = (String) message.splitCommand(1);
                    Integer portNum = (Integer) message.splitCommand(2);
                    Object info = message.splitCommand(3);

                    if (info instanceof AuctionInfo) {
                        AuctionInfo auctionInfo = (AuctionInfo) info;
                        this.auctionHouseAddress = new AuctionHouseAddress(ipAddress, portNum);
                        Bank.auctionHouseAddressHashMap.put(auctionInfo, auctionHouseAddress);
                        System.out.println("Registered auction house with " + auctionInfo.getItems().size() + " items");
                    }

                    out.writeObject(new Message("AddressRegistered"));
                    out.flush();
                    break;

                case "Block Funds":
                    this.agentAccount = Bank.accountHashMap.get(message.splitCommand(1));
                    int amount = (Integer) message.splitCommand(2);
                    System.out.println("Blocking funds from account " +
                            this.agentAccount.getAccountNum() +
                            " amount: " + amount);
                    this.agentAccount.setBlockFunds(amount);
                    out.writeObject(new Message("FundsBlocked"));
                    out.flush();
                    break;

                case "Unblock Funds":
                    this.agentAccount = Bank.accountHashMap.get(message.splitCommand(1));
                    amount = (Integer) message.splitCommand(2);
                    System.out.println("Unblocking funds from account " +
                            this.agentAccount.getAccountNum() +
                            " amount: " + amount);
                    this.agentAccount.setUnBlockFunds(amount);
                    out.writeObject(new Message("FundsUnblocked"));
                    out.flush();
                    break;

                case "Terminates":
                    Bank.accountHashMap.remove(account.getAccountNum());
                    Bank.auctionHouseAddressHashMap.values().remove(auctionHouseAddress);
                    out.writeObject(new Message("Terminated"));
                    out.flush();
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error processing auction message: " + e.getMessage());
            e.printStackTrace();
            out.writeObject(new Message("Error", e.getMessage()));
            out.flush();
        }
    }

    @Override
    public void run() {
        System.out.println("AuctionHandler started");
        try {
            while (true) {
                Message message = (Message) in.readObject();
                if (message.getCommand().equals("Terminates")) {
                    processMessage(message);
                    break;
                } else {
                    processMessage(message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in auction handler: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}