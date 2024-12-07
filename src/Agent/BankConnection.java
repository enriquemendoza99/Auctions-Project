package Agent;

import constants.Message;
import java.io.*;
import java.net.*;

public class BankConnection {
    private Socket bankSocket;
    private ObjectOutputStream bankOut;
    private ObjectInputStream bankIn;
    private Agent agent;

    public BankConnection(int bankPort, Agent agent) throws IOException {
        this.agent = agent;
        connect(bankPort);
    }

    private void connect(int bankPort) throws IOException {
        bankSocket = new Socket("localhost", bankPort);
        bankOut = new ObjectOutputStream(bankSocket.getOutputStream());
        bankIn = new ObjectInputStream(bankSocket.getInputStream());
        new Thread(new BankListener()).start();
    }

    public int register(String name, int initialBalance) throws IOException, ClassNotFoundException {
        bankOut.writeObject(new Message("NewAgent"));
        bankOut.writeObject(new Message("CreateNewAccount", initialBalance, name));
        Message response = (Message) bankIn.readObject();
        return (Integer) response.splitCommand(1);
    }

    public void sendMessage(Message message) throws IOException {
        bankOut.writeObject(message);
    }

    public Message receiveMessage() throws IOException, ClassNotFoundException {
        return (Message) bankIn.readObject();
    }

    public void close() throws IOException {
        bankOut.writeObject(new Message("Terminates"));
        bankSocket.close();
    }

    private class BankListener implements Runnable {
        @Override
        public void run() {
            while (agent.isRunning()) {
                try {
                    Message message = (Message) bankIn.readObject();
                    handleBankMessage(message);
                } catch (Exception e) {
                    if (agent.isRunning()) e.printStackTrace();
                }
            }
        }

        private void handleBankMessage(Message message) {
            switch (message.getCommand()) {
                case "winner":
                    System.out.println("Won auction for item: " + message.splitCommand(1));
                    break;
                case "outbid":
                    System.out.println("Outbid on item: " + message.splitCommand(1));
                    break;
            }
        }
    }
}
