package Agent;

import constants.Message;
import java.io.*;
import java.net.*;

public class BankConnection {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public BankConnection(String host, int bankPort) throws IOException {
        this.socket = new Socket(host, bankPort);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void sendMessage(Message message) throws IOException {
        out.writeObject(message);
    }

    public Message receiveMessage() throws IOException, ClassNotFoundException {
        return (Message) in.readObject();
    }

    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }
}