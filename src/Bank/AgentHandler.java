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
            // Send initial response to confirm connection
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

                    out.writeObject(new Message("AccountCreated", account.getAccountNum()));
                    out.flush();
                    System.out.println("Account created with number: " + account.getAccountNum());
                    break;

                // ... rest of the cases remain the same ...
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
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
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}