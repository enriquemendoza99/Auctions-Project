package Bank;

import constants.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class AgentHandler extends Thread {
    private Socket socket;
    private Account account;

    public AgentHandler(Socket socket) {
        this.socket = socket;
    }

    private void processMessage(Message message) throws IOException, ClassNotFoundException {
        switch (message.getCommand()) {
            case "CreateNewAccount" :
                int initFunds = (Integer) message.splitCommand(1);
                String nameOfAgent = (String) message.splitCommand(2);
                this.account = new Account(initFunds, Bank.accountHashMap);
                this.account.setName(nameOfAgent);
                Bank.accountHashMap.put(account.getAccountNum(), account);
                sendMessageToAgent(new Message("", account.getAccountNum()));
                break;

            case "SendBlockedMoneyToAuction" :
                int accountNumOfAuction = (Integer) message.splitCommand(1);
                Account auctionAccount = Bank.accountHashMap.get(accountNumOfAuction);
                auctionAccount.depositMoney(account.getHeld());
                account.setNewBalance();
                break;

            case "ViewCurrentAuctions" :
                sendMessageToAgent(new Message("", Bank.auctionHouseAddressHashMap));
                break;

            case "availableBalance" :
                sendMessageToAgent(new Message("", account.getMoney()));
                break;

            case "totalBalance" :
                sendMessageToAgent(new Message("", account.getMoney()));
                break;

            case "Terminates" :
                Bank.accountHashMap.remove(account.getAccountNum());
                break;
        }
    }

    private void sendMessageToAgent(Message message) throws IOException {
        ObjectOutputStream objOutput = null;

        try {
            objOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        objOutput.writeObject(message);
    }

    public void run() {
        while (true) {
            ObjectInputStream objectInputStream = null;

            try {
                objectInputStream = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Message message = (Message) objectInputStream.readObject();
                if(message.getCommand().equals("Terminates")) {
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
