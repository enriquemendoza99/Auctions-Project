package Bank;

import constants.Message;
import java.io.*;
import java.net.Socket;

public class AgentHandler extends Thread {
    private Socket socket;
    private Account account;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public AgentHandler(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        System.out.println("AgentHandler: Using existing streams");
        try {
            out.writeObject(new Message("Ready"));
            out.flush();
            System.out.println("AgentHandler: Sent ready message");
        } catch (IOException e) {
            System.err.println("Error in AgentHandler constructor: " + e.getMessage());
        }
    }

    private void processMessage(Message message) throws IOException {
        System.out.println("Processing message: " + message.getCommand());

        try {
            switch (message.getCommand()) {
                case "CreateNewAccount":
                    int initFunds = (Integer) message.splitCommand(1);
                    String nameOfAgent = (String) message.splitCommand(2);
                    System.out.println("Creating account for " + nameOfAgent + " with " + initFunds);

                    this.account = new Account(initFunds, Bank.accountHashMap);
                    this.account.setName(nameOfAgent);
                    Bank.accountHashMap.put(account.getAccountNum(), account);

                    Message response = new Message("AccountCreated", account.getAccountNum());
                    out.writeObject(response);
                    out.flush();
                    System.out.println("Account created with number: " + account.getAccountNum());
                    break;

                case "SendBlockedMoneyToAuction":
                    int accountNumOfAuction = (Integer) message.splitCommand(1);
                    Account auctionAccount = Bank.accountHashMap.get(accountNumOfAuction);
                    if (auctionAccount != null) {
                        auctionAccount.depositMoney(account.getHeld());
                        account.setNewBalance();
                        out.writeObject(new Message("TransferComplete"));
                    } else {
                        out.writeObject(new Message("Error", "Auction account not found"));
                    }
                    out.flush();
                    break;

                case "ViewCurrentAuctions":
                    System.out.println("Sending auction list to agent");
                    out.writeObject(new Message("AuctionList", Bank.auctionHouseAddressHashMap));
                    out.flush();
                    System.out.println("Sent auction list: " + Bank.auctionHouseAddressHashMap.size() + " auctions");
                    break;

                case "availableBalance":
                    System.out.println("Sending balance information");
                    out.writeObject(new Message("Balance", account.getMoney()));
                    out.flush();
                    System.out.println("Sent balance: " + account.getMoney());
                    break;

                case "checkBlocked":
                    System.out.println("Checking blocked funds");
                    out.writeObject(new Message("BlockedAmount", account.getHeld()));
                    out.flush();
                    System.out.println("Sent blocked amount: " + account.getHeld());
                    break;

                case "blockFunds":
                    int amount = (Integer) message.splitCommand(1);
                    if (amount <= account.getUsableMoney()) {
                        account.setBlockFunds(amount);
                        out.writeObject(new Message("FundsBlocked", amount));
                    } else {
                        out.writeObject(new Message("InsufficientFunds"));
                    }
                    out.flush();
                    break;

                case "unblockFunds":
                    amount = (Integer) message.splitCommand(1);
                    account.setUnBlockFunds(amount);
                    out.writeObject(new Message("FundsUnblocked", amount));
                    out.flush();
                    break;

                case "Terminates":
                    System.out.println("Processing termination for account: " + account.getAccountNum());
                    Bank.accountHashMap.remove(account.getAccountNum());
                    out.writeObject(new Message("Terminated"));
                    out.flush();
                    break;

                default:
                    System.out.println("Unknown command received: " + message.getCommand());
                    out.writeObject(new Message("Error", "Unknown command"));
                    out.flush();
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
            try {
                out.writeObject(new Message("Error", e.getMessage()));
                out.flush();
            } catch (IOException ioe) {
                System.err.println("Failed to send error message: " + ioe.getMessage());
            }
        }
    }

    @Override
    public void run() {
        System.out.println("AgentHandler started");
        try {
            while (true) {
                Message message = (Message) in.readObject();
                System.out.println("Received message: " + message.getCommand());

                if (message.getCommand().equals("Terminates")) {
                    processMessage(message);
                    break;
                } else {
                    processMessage(message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in agent handler: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Closing agent handler connection");
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}