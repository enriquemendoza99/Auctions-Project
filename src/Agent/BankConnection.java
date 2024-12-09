package Agent;

import constants.Message;
import java.io.*;
import java.net.Socket;

public class BankConnection {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public BankConnection(String host, int port) throws IOException {
        try {
            this.socket = new Socket(host, port);
            System.out.println("Socket connected to bank");

            this.out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            System.out.println("Output stream created");

            this.in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Input stream created");

            out.writeObject(new Message("NewAgent"));
            out.flush();
            System.out.println("Sent NewAgent message to bank");

            Message response = (Message) in.readObject();
            if (!"Ready".equals(response.getCommand())) {
                throw new IOException("Unexpected response: " + response.getCommand());
            }
            System.out.println("Bank connection ready");

        } catch (Exception e) {
            System.err.println("Error in BankConnection: " + e.getMessage());
            throw new IOException("Failed to establish bank connection", e);
        }
    }

    public int createAccount(String agentName, int initialFunds) throws IOException, ClassNotFoundException {
        try {
            System.out.println("Sending CreateNewAccount message");
            out.writeObject(new Message("CreateNewAccount", initialFunds, agentName));
            out.flush();

            System.out.println("Waiting for response from bank...");
            Message response = (Message) in.readObject();
            System.out.println("Received response from bank: " + response.getCommand());

            if ("AccountCreated".equals(response.getCommand())) {
                Integer accountNum = (Integer) response.splitCommand(1);
                System.out.println("Account created successfully with number: " + accountNum);
                return accountNum;
            } else {
                throw new IOException("Failed to create account: " + response.getCommand());
            }
        } catch (Exception e) {
            System.err.println("Error creating account: " + e.getMessage());
            throw e;
        }
    }

    public void close() {
        try {
            out.writeObject(new Message("Terminates"));
            out.flush();
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}