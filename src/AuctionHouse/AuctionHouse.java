package AuctionHouse;

import constants.AuctionHouseAddress;
import constants.Message;
import java.io.*;
import java.net.*;
import java.util.*;

public class AuctionHouse {
    private ServerSocket serverSocket;
    private Socket bankSocket;
    private ObjectOutputStream bankOut;
    private ObjectInputStream bankIn;
    private List<Item> items;
    private int accountNum;
    private HashMap<String, Timer> bidTimers;
    private boolean running;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java AuctionHouse.AuctionHouse BANK_PORT YOUR_PORT");
            System.exit(0);
        }
        try {
            System.out.println("Starting Auction House...");
            AuctionHouse auctionHouse = new AuctionHouse();
            auctionHouse.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } catch (Exception e) {
            System.out.println("Error starting Auction House: ");
            e.printStackTrace();
        }
    }

    public AuctionHouse() {
        items = new ArrayList<>();
        bidTimers = new HashMap<>();
        running = true;
        initializeItems();
    }

    private void initializeItems() {
        System.out.println("Initializing items for auction...");
        items.add(new Item("Vintage Watch", "Rare 1950s chronograph", 500));
        items.add(new Item("Antique Vase", "Ming dynasty porcelain", 1000));
        items.add(new Item("Rare Painting", "19th century landscape", 2000));
    }

    public void start(int bankPort, int ownPort) throws IOException, ClassNotFoundException {
        System.out.println("Connecting to bank on port " + bankPort);
        bankSocket = new Socket("localhost", bankPort);
        bankOut = new ObjectOutputStream(bankSocket.getOutputStream());
        bankIn = new ObjectInputStream(bankSocket.getInputStream());

        System.out.println("Registering with bank...");
        bankOut.writeObject(new Message("NewAuctionHouse"));
        bankOut.writeObject(new Message("Create New Account"));

        Message response = (Message) bankIn.readObject();
        accountNum = (Integer) response.splitCommand(1);
        System.out.println("Received account number: " + accountNum);

        serverSocket = new ServerSocket(ownPort);
        String hostAddress = InetAddress.getLocalHost().getHostAddress();

        System.out.println("Sending address info to bank: " + hostAddress + ":" + ownPort);
        bankOut.writeObject(new Message("Auction Address", hostAddress, ownPort));
        System.out.println("Auction House started on port " + ownPort);

        System.out.println("Initial items:");
        for (Item item : items) {
            System.out.println(" - " + item.getName() + " (Minimum bid: $" + item.getMinimumBid() + ")");
        }

        System.out.println("Waiting for agent connections...");

        while (running) {
            try {
                Socket agentSocket = serverSocket.accept();
                System.out.println("New agent connected from: " + agentSocket.getInetAddress());
                new Thread(new AgentHandler(agentSocket, this)).start();
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        }
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    public void processBid(String itemId, int agentAccount, int bidAmount, AgentHandler handler) {
        Item item = findItem(itemId);
        if (item == null || bidAmount <= item.getCurrentBid()) {
            try {
                handler.sendMessage(new Message("rejection"));
                System.out.println("Bid rejected: " + bidAmount + " for item " + itemId);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            System.out.println("Processing bid: " + bidAmount + " for item " + itemId);
            bankOut.writeObject(new Message("Block Funds", agentAccount, bidAmount));
            item.setCurrentBid(bidAmount);
            item.setCurrentBidder(agentAccount);
            handler.sendMessage(new Message("acceptance"));

            Timer oldTimer = bidTimers.get(itemId);
            if (oldTimer != null) {
                oldTimer.cancel();
            }

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        handleWinningBid(item);
                        handler.sendMessage(new Message("winner", item.getId()));
                        System.out.println("Auction completed: Item " + itemId + " sold for " + bidAmount);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 30000);

            bidTimers.put(itemId, timer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleWinningBid(Item item) {
        items.remove(item);
        if (items.size() < 3) {
            items.add(new Item("New Item", "Recently added auction item", 500));
            System.out.println("Added new item to maintain minimum inventory");
        }
    }

    private Item findItem(String itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }
}
