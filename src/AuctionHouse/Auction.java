package AuctionHouse;

import java.util.Timer;
import java.util.TimerTask;

public class Auction {
    private static final int AUCTION_DURATION = 30000; // 30 seconds
    private Item item;
    private Timer timer;
    private AuctionCallback callback;
    private boolean active;

    public interface AuctionCallback {
        void onAuctionComplete(Item item);
        void onBidAccepted(Item item);
    }

    public Auction(Item item, AuctionCallback callback) {
        this.item = item;
        this.callback = callback;
        this.active = true;
    }

    public void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (active) {
                    completeAuction();
                }
            }
        }, AUCTION_DURATION);
    }

    public void placeBid(int bidder, int amount) {
        if (!active || amount <= item.getCurrentBid()) {
            return;
        }

        item.setCurrentBid(amount);
        item.setCurrentBidder(bidder);
        callback.onBidAccepted(item);

        // Reset timer for new bid
        if (timer != null) {
            timer.cancel();
        }
        startTimer();
    }

    public void completeAuction() {
        if (!active) return;

        active = false;
        if (timer != null) {
            timer.cancel();
        }
        item.setSold(true);
        callback.onAuctionComplete(item);
    }

    public boolean isActive() {
        return active;
    }

    public Item getItem() {
        return item;
    }
}
