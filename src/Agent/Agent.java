package Agent;

import constants.Message;
import constants.AuctionHouseAddress;
import java.io.*;
import java.net.*;
import java.util.*;

public class Agent {
    private Socket bankSocket;
    private HashMap<String, Socket> auctionSockets;
    private ObjectOutputStream bankOut;
    private ObjectInputStream bankIn;
    private int accountNum;
    private Scanner scanner;
    private boolean isAuto;
    private boolean running;
    private BankConnection bankConnection;
    private AuctionManager auctionManager;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java Agent.Agent BANK_PORT NAME INITIAL_BALANCE [auto]");
            System.exit(0);
        }

        try {
            System.out.println("Starting Agent...");
            boolean isAuto = args.length > 3 && args[3].equals("auto");
            Agent agent = new Agent(isAuto);
            System.out.println("Connecting to bank on port " + args[0]);
            agent.start(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]));
        } catch (Exception e) {
            System.out.println("Error starting Agent:");
            e.printStackTrace();
        }
    }

    public Agent(boolean isAuto) {
        System.out.println("Initializing Agent...");
        this.isAuto = isAuto;
        this.auctionSockets = new HashMap<>();
        this.listeners = new HashMap<>();
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    public void start(int bankPort, String name, int initialBalance) throws IOException, ClassNotFoundException {
        try {
            System.out.println("Connecting to bank at localhost:" + bankPort);
            bankSocket = new Socket("localhost", bankPort);
            System.out.println("Connected to bank");

            bankOut = new ObjectOutputStream(bankSocket.getOutputStream());
            bankIn = new ObjectInputStream(bankSocket.getInputStream());

            System.out.println("Registering with bank...");
            bankOut.writeObject(new Message("NewAgent"));
            bankOut.writeObject(new Message("CreateNewAccount", initialBalance, name));

            Message response = (Message) bankIn.readObject();
            accountNum = (Integer) response.splitCommand(1);
            System.out.println("Account created with number: " + accountNum);

            if (isAuto) {
                runAutoBidder();
            } else {
                showMenu();
            }
        } catch (Exception e) {
            System.out.println("Error during startup:");
            e.printStackTrace();
            throw e;
        }
    }

    private void showMenu() {
        while (running) {
            try {
                System.out.println("\n=== Agent Menu ===");
                System.out.println("1. View Balance");
                System.out.println("2. View Auction Houses");
                System.out.println("3. View Items");
                System.out.println("4. Place Bid");
                System.out.println("5. Exit");
                System.out.print("\nEnter choice (1-5): ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                handleMenuChoice(choice);
            } catch (Exception e) {
                System.out.println("Error processing menu choice:");
                e.printStackTrace();
            }
        }
    }

    private void handleMenuChoice(int choice) throws IOException, ClassNotFoundException {
        switch (choice) {
            case 1:
                checkBalance();
                break;
            case 2:
                viewAuctionHouses();
                break;
            case 3:
                viewItems();
                break;
            case 4:
                placeBid();
                break;
            case 5:
                exit();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private void checkBalance() throws IOException, ClassNotFoundException {
        System.out.println("Checking balance...");
        bankOut.writeObject(new Message("availableBalance"));
        Message response = (Message) bankIn.readObject();
        System.out.println("Available Balance: $" + response.splitCommand(1));
    }

    private void viewAuctionHouses() throws IOException, ClassNotFoundException {
        System.out.println("Requesting auction house list...");
        bankOut.writeObject(new Message("ViewCurrentAuctions"));
        Message response = (Message) bankIn.readObject();
        HashMap<?, AuctionHouseAddress> auctions =
                (HashMap<?, AuctionHouseAddress>) response.splitCommand(1);

        if (auctions.isEmpty()) {
            System.out.println("No auction houses available.");
            return;
        }

        System.out.println("\nAvailable Auction Houses:");
        for (Map.Entry<?, AuctionHouseAddress> entry : auctions.entrySet()) {
            AuctionHouseAddress addr = entry.getValue();
            String addressKey = addr.getIpAddress() + ":" + addr.getPortNum();
            System.out.println(addressKey);
            connectToAuctionHouse(addr);
        }
    }

    private void connectToAuctionHouse(AuctionHouseAddress address) throws IOException {
        String addressKey = address.getIpAddress() + ":" + address.getPortNum();
        if (!auctionSockets.containsKey(addressKey)) {
            System.out.println("Connecting to auction house at " + addressKey);
            Socket socket = new Socket(address.getIpAddress(), address.getPortNum());
            auctionSockets.put(addressKey, socket);
            AuctionListener listener = new AuctionListener(socket);
            listeners.put(socket, listener);
            new Thread(listener).start();
            System.out.println("Connected to auction house at " + addressKey);
        }
    }

    private void viewItems() throws IOException {
        if (auctionSockets.isEmpty()) {
            System.out.println("No auction houses connected. Please view auction houses first.");
            return;
        }

        System.out.println("Requesting items from all auction houses...");
        for (Map.Entry<String, Socket> entry : auctionSockets.entrySet()) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(entry.getValue().getOutputStream());
                out.writeObject(new Message("GetItems"));
            } catch (IOException e) {
                System.out.println("Error requesting items from " + entry.getKey());
            }
        }
    }

    private void placeBid() throws IOException {
        if (auctionSockets.isEmpty()) {
            System.out.println("No auction houses connected. Please view auction houses first.");
            return;
        }

        System.out.println("\nAvailable auction houses:");
        for (String address : auctionSockets.keySet()) {
            System.out.println(address);
        }

        System.out.print("Enter auction house address (IP:PORT): ");
        String auctionAddress = scanner.nextLine();
        System.out.print("Enter item ID: ");
        String itemId = scanner.nextLine();
        System.out.print("Enter bid amount: $");
        int bidAmount = scanner.nextInt();

        Socket socket = auctionSockets.get(auctionAddress);
        if (socket != null && socket.isConnected()) {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(new Message("Bid", itemId, accountNum, bidAmount));
            System.out.println("Bid sent. Awaiting response...");
        } else {
            System.out.println("Invalid auction house address or connection lost");
        }
    }

    private void exit() throws IOException {
        System.out.println("Exiting...");
        running = false;

        // Close bank connection
        bankOut.writeObject(new Message("Terminates"));
        bankSocket.close();

        // Close auction house connections
        for (Map.Entry<Socket, AuctionListener> entry : listeners.entrySet()) {
            entry.getValue().stop();
            entry.getKey().close();
        }

        System.out.println("Goodbye!");
        System.exit(0);
    }

    private void runAutoBidder() {
        System.out.println("Starting auto-bidder mode...");
        Random random = new Random();

        while (running) {
            try {
                Thread.sleep(5000);
                viewAuctionHouses();
                viewItems();
                // Auto-bidding logic could be implemented here
            } catch (Exception e) {
                if (running) {
                    System.out.println("Error in auto-bidder:");
                    e.printStackTrace();
                }
            }
        }
    }
    public class BankConnection {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        public BankConnection(Socket socket) throws IOException {
            this.socket = socket;
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
            socket.close();
        }
    }
    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getAccountNum() {
        return accountNum;
    }

    public BankConnection getBankConnection() {
        return bankConnection;
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }
}
