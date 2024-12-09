package Bank;

import constants.AuctionHouseAddress;
import constants.Message;
import java.io.*;
import java.net.Socket;
import java.util.Random;

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

        switch(message.getCommand()) {
            case "Create New Account":
                this.account = new Account(0, Bank.accountHashMap);
                Bank.accountHashMap.put(account.getAccountNum(), account);
                out.writeObject(new Message("AccountCreated", account.getAccountNum()));
                out.flush();
                break;

            case "Auction Address":
                Random rand = new Random();
                String auctionID = "AuctionHouse" + rand.nextInt(10000);
                this.auctionHouseAddress = new AuctionHouseAddress(
                        (String) message.splitCommand(1),
                        (Integer) message.splitCommand(2)
                );
                Bank.auctionHouseAddressHashMap.put(
                        new AuctionInfo(account.getAccountNum(), auctionID),
                        auctionHouseAddress
                );
                out.writeObject(new Message("AddressRegistered"));
                out.flush();
                break;

            case "Block Funds":
                this.agentAccount = Bank.accountHashMap.get(message.splitCommand(1));
                System.out.println("Blocking funds from account " +
                        this.agentAccount.getAccountNum() +
                        " amount: " + message.splitCommand(2));
                this.agentAccount.setBlockFunds((Integer) message.splitCommand(2));
                out.writeObject(new Message("FundsBlocked"));
                out.flush();
                break;

            case "Unblock Funds":
                this.agentAccount = Bank.accountHashMap.get(message.splitCommand(1));
                System.out.println("Unblocking funds from account " +
                        this.agentAccount.getAccountNum() +
                        " amount: " + message.splitCommand(2));
                this.agentAccount.setUnBlockFunds((Integer) message.splitCommand(2));
                out.writeObject(new Message("FundsUnblocked"));
                out.flush();
                break;

            case "Check Balance":
                int balance = Bank.accountHashMap.get(message.splitCommand(1)).getMoney();
                out.writeObject(new Message("Balance", balance));
                out.flush();
                break;

            case "Terminates":
                Bank.accountHashMap.remove(account.getAccountNum());
                Bank.auctionHouseAddressHashMap.remove(auctionHouseAddress);
                out.writeObject(new Message("Terminated"));
                out.flush();
                break;
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