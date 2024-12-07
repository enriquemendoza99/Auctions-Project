package constants;

/**
 * Represents the possible outcomes of placing a bid
 */
public enum StatusBid {
    ACCEPTED,// Bid was successful
    NOFUNDS,// Not enough money in bank account
    BIDLOWER,// Bid amount was lower than current bid
    ITEMSOLD,
}
