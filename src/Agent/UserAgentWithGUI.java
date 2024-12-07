package Agent;

import constants.Message;
import constants.StatusBid;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public class UserAgentWithGUI extends UserAgent {
    private final AgentGUI gui;
    private final String bankHost;
    private final int bankPort;

    public UserAgentWithGUI(String name, double initialBalance,
                            String bankHost, int bankPort, AgentGUI gui) {
        super(name, initialBalance);
        this.gui = gui;
        this.bankHost = bankHost;
        this.bankPort = bankPort;
    }

    @Override
    public void run() {
        try {
            connectToBank(bankHost, bankPort);
            updateAuctionHouses();
            updateBalanceDisplay();
        } catch (Exception e) {
            gui.addLogMessage("Error starting agent: " + e.getMessage());
        }
    }

    private void updateBalanceDisplay() {
        try {
            double available = getAvailableBalance();
            double total = getTotalBalance();
            gui.updateBalance(total, available);
        } catch (Exception e) {
            gui.addLogMessage("Error updating balance: " + e.getMessage());
        }
    }

    private double getAvailableBalance() throws IOException, ClassNotFoundException {
        bankOut.writeObject(new Message("availableBalance"));
        Message response = (Message) bankIn.readObject();
        return (Double) response.splitCommand(1);
    }

    private double getTotalBalance() throws IOException, ClassNotFoundException {
        bankOut.writeObject(new Message("totalBalance"));
        Message response = (Message) bankIn.readObject();
        return (Double) response.splitCommand(1);
    }

    public void refreshAuctions() {
        try {
            updateAuctionHouses();
            for (String auctionKey : auctionOutputs.keySet()) {
                ObjectOutputStream out = auctionOutputs.get(auctionKey);
                out.writeObject(new Message("GetItems"));
            }
        } catch (Exception e) {
            gui.addLogMessage("Error refreshing auctions: " + e.getMessage());
        }
    }

    public void tryPlaceBid(String auctionKey, String itemId, double amount) {
        try {
            ObjectOutputStream out = auctionOutputs.get(auctionKey);
            if (out != null) {
                out.writeObject(new Message("PlaceBid", itemId, amount));
                gui.addLogMessage(String.format("Placing bid of $%.2f on item %s",
                        amount, itemId));
            }
        } catch (IOException e) {
            gui.addLogMessage("Error placing bid: " + e.getMessage());
        }
    }

    @Override
    protected void handleAuctionMessage(Message message, String auctionKey) {
        switch (message.getCommand()) {
            case "ItemList":
                @SuppressWarnings("unchecked")
                List<AuctionHouse.AuctionItem> items =
                        (List<AuctionHouse.AuctionItem>) message.splitCommand(1);
                gui.updateItems(auctionKey, items);
                break;

            case "BidResult":
                StatusBid status = (StatusBid) message.splitCommand(1);
                gui.addLogMessage("Bid result: " + status);
                updateBalanceDisplay();
                break;

            case "BidUpdate":
                String itemId = (String) message.splitCommand(1);
                double newBid = (Double) message.splitCommand(2);
                int bidderId = (Integer) message.splitCommand(3);
                gui.addLogMessage(String.format("New bid on item %s: $%.2f by agent %d",
                        itemId, newBid, bidderId));
                break;

            case "Winner":
                gui.addLogMessage("Won auction for item " + message.splitCommand(1) +
                        " for $" + message.splitCommand(2));
                updateBalanceDisplay();
                break;
        }
    }
}

