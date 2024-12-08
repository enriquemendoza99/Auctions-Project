package Bank;

import constants.AuctionHouseAddress;
import constants.Message;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

public class AuctionHandler extends Thread {
    private Socket socket;
    private Account account;
    private Account agentAccount;
    private AuctionHouseAddress auctionHouseAddress;

    public AuctionHandler(Socket socket) {
        this.socket = socket;
    }

    private void processMessage(Message message) throws IOException {
        switch(message.getCommand()) {
            case "Check Balance" :
                ObjectOutputStream checkBalance = new ObjectOutputStream(socket.getOutputStream());
                int balance = (Bank.accountHashMap.get(message.splitCommand(1)).getMoney());
                checkBalance.writeObject(new Message("", balance));
                break;

            case "Create New Account" :
                this.account = new Account(0, Bank.accountHashMap);
                Bank.accountHashMap.put(account.getAccountNum(), account);
                ObjectOutputStream sender = new ObjectOutputStream(socket.getOutputStream());
                sender.writeObject(new Message("Account Number", account.getAccountNum()));
                break;

            case "Auction Address" :
                Random rand = new Random();
                String auctionID = "AuctionHouse" + rand.nextInt(10000);
                this.auctionHouseAddress = new AuctionHouseAddress((String) message.splitCommand(1),
                        (Integer) message.splitCommand(2));
                Bank.auctionHouseAddressHashMap.put(new AuctionInfo(account.getAccountNum(), auctionID),
                        auctionHouseAddress);
                break;

            case "Block Funds" :
                this.agentAccount = Bank.accountHashMap.get(message.splitCommand(1));
                this.agentAccount = Bank.accountHashMap.get(message.splitCommand(1));
                System.out.println("Block funds from account number " + this.agentAccount.getAccountNum() +
                        " with amount" + message.splitCommand(2));
                this.agentAccount.setBlockFunds((int) message.splitCommand(2));
                break;

            case "Unblock Funds" :
                this.agentAccount = Bank.accountHashMap.get(message.splitCommand(1));
                this.agentAccount.setUnBlockFunds((Integer) message.splitCommand(2));
                System.out.println("Unblock funds of account number " +
                        this.agentAccount.getAccountNum() +
                        " with amount" + message.splitCommand(2));
                this.agentAccount.setUnBlockFunds((Integer) message.splitCommand(2));
                break;
            case "Terminates" :
                Bank.accountHashMap.remove(account.getAccountNum());
                Bank.auctionHouseAddressHashMap.remove(auctionHouseAddress);
                break;
        }
    }

    public void run() {
        while(true) {
            ObjectInputStream objectInputStream = null;
            try {
                objectInputStream = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                 Message message = (Message) objectInputStream.readObject();
                 if (message.getCommand().equals("Terminates")) {
                     processMessage(message);
                     break;
                 } else {
                     processMessage(message);
                 }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
