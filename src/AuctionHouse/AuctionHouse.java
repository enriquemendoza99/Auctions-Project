package AuctionHouse;

import constants.Message;
import constants.StatusBid;
import constants.AuctionHouseAddress;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class AuctionHouse extends Thread {
    private ServerSocket auctionSocket;
    private Socket bankSocket;
    private ObjectOutputStream bankOut;
    private ObjectInputStream bankIn;
    private final List<AuctionItem> items;
    private final Map<Integer, ClientHandler> clientHandlers;
    private int bankAccountNumber;
    private final ScheduledExecutorService scheduler;
    private static final int BID_TIMEOUT_SECONDS = 30;

    public AuctionHouse() {
        this.items = new CopyOnWriteArrayList<>();
        this.clientHandlers = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start(String bankHost, int bankPort, int auctionPort) throws IOException {
        connectToBank(bankHost, bankPort);
        auctionSocket = new ServerSocket(auctionPort);
        setupInitialItems();
        startAuctionMonitor();

        while (true) {
            try {
                Socket clientSocket = auctionSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            } catch (IOException e) {
                System.out.println("Error accepting client: " + e.getMessage());
            }
        }
    }

    private void connectToBank(String host, int port) throws IOException {
        bankSocket = new Socket(host, port);
        bankOut = new ObjectOutputStream(bankSocket.getOutputStream());
        bankIn = new ObjectInputStream(bankSocket.getInputStream());

        bankOut.writeObject(new Message("NewAuctionHouse"));
        bankOut.writeObject(new Message("Create New Account"));

        try {
            Message response = (Message) bankIn.readObject();
            bankAccountNumber = (Integer) response.splitCommand(1);

            bankOut.writeObject(new Message("Auction Address",
                    auctionSocket.getInetAddress().getHostAddress(),
                    auctionSocket.getLocalPort()));

        } catch (ClassNotFoundException e) {
            throw new IOException("Error registering with bank", e);
        }
    }

    private void setupInitialItems() {
        items.add(new AuctionItem("1", "Vintage Watch", 1000.0));
        items.add(new AuctionItem("2", "Rare Painting", 5000.0));
        items.add(new AuctionItem("3", "First Edition Book", 2000.0));
    }

    private void startAuctionMonitor() {
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            for (AuctionItem item : items) {
                if (item.isAvailable() &&
                        (currentTime - item.getLastBidTime()) > BID_TIMEOUT_SECONDS * 1000) {
                    handleAuctionComplete(item);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void handleAuctionComplete(AuctionItem item) {
        item.markAsSold();

        ClientHandler winner = clientHandlers.get(item.getCurrentBidderId());
        if (winner != null) {
            try {
                winner.sendMessage(new Message("Winner", item.getItemId(), item.getCurrentBid()));
            } catch (IOException e) {
                System.out.println("Error notifying winner: " + e.getMessage());
            }
        }

        addNewItem();
    }

    private void addNewItem() {
        Random random = new Random();
        String[] items = {
                "Antique Vase", "Gold Coin", "Vintage Camera",
                "Classic Vinyl Record", "Rare Comic Book", "Ancient Map"
        };
        String newItem = items[random.nextInt(items.length)];
        double basePrice = 500 + random.nextInt(5000);

        this.items.add(new AuctionItem(
                UUID.randomUUID().toString(),
                newItem,
                basePrice
        ));
    }

    private class ClientHandler extends Thread {
        private final Socket socket;
        private final ObjectInputStream in;
        private final ObjectOutputStream out;
        private int clientId;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        }

        public void sendMessage(Message message) throws IOException {
            out.writeObject(message);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Message message = (Message) in.readObject();
                    processMessage(message);
                }
            } catch (Exception e) {
                System.out.println("Client disconnected: " + e.getMessage());
                clientHandlers.remove(clientId);
            }
        }

        private void processMessage(Message message) throws IOException {
            switch (message.getCommand()) {
                case "Register":
                    handleRegister(message);
                    break;
                case "GetItems":
                    sendMessage(new Message("ItemList", new ArrayList<>(items)));
                    break;
                case "PlaceBid":
                    handleBid(message);
                    break;
            }
        }

        private void handleRegister(Message message) {
            clientId = (Integer) message.splitCommand(1);
            clientHandlers.put(clientId, this);
            try {
                sendMessage(new Message("RegisterConfirmed"));
            } catch (IOException e) {
                System.out.println("Error confirming registration: " + e.getMessage());
            }
        }

        private void handleBid(Message message) throws IOException {
            String itemId = (String) message.splitCommand(1);
            double bidAmount = (Double) message.splitCommand(2);

            AuctionItem item = findItem(itemId);
            if (item == null) {
                sendMessage(new Message("BidRejected", "Item not found"));
                return;
            }

            try {
                bankOut.writeObject(new Message("Check Balance", clientId));
                Message bankResponse = (Message) bankIn.readObject();
                double availableFunds = (Double) bankResponse.splitCommand(1);

                if (availableFunds < bidAmount) {
                    sendMessage(new Message("BidResult", StatusBid.NOFUNDS));
                    return;
                }

                StatusBid status = item.placeBid(bidAmount, clientId);
                if (status == StatusBid.ACCEPTED) {
                    bankOut.writeObject(new Message("Block Funds", clientId, bidAmount));
                    broadcastBid(item);
                }
                sendMessage(new Message("BidResult", status));
            } catch (ClassNotFoundException e) {
                System.out.println("Error processing bid: " + e.getMessage());
            }
        }

        private AuctionItem findItem(String itemId) {
            return items.stream()
                    .filter(item -> item.getItemId().equals(itemId))
                    .findFirst()
                    .orElse(null);
        }

        private void broadcastBid(AuctionItem item) {
            Message bidUpdate = new Message("BidUpdate",
                    item.getItemId(),
                    item.getCurrentBid(),
                    item.getCurrentBidderId());

            for (ClientHandler client : clientHandlers.values()) {
                try {
                    client.sendMessage(bidUpdate);
                } catch (IOException e) {
                    System.out.println("Error broadcasting bid: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java AuctionHouse <bank-host> <bank-port> <auction-port>");
            System.exit(1);
        }

        try {
            AuctionHouse auctionHouse = new AuctionHouse();
            String bankHost = args[0];
            int bankPort = Integer.parseInt(args[1]);
            int auctionPort = Integer.parseInt(args[2]);

            auctionHouse.start(bankHost, bankPort, auctionPort);
        } catch (Exception e) {
            System.out.println("Error starting auction house: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
