package constants;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * this object is used to communicate between programs.
 * Any amount of serializable object can be passes to the constructor.
 * The command is the first item passed, which is to they signify the recipient
 * of the message.
 */
public class Message implements Serializable {
    /**
     * MESSAGE OPTIONS
     * Format: [Command][Arguments]: Sender/Receiver
     *
     * General:
     * - "" as first argument indicates no command.
     *
     * Commands:
     * - [Terminates][]: Bank/Agent or AuctionHouse
     *   Notify to end connection with agent or auction house.
     * - [CheckBalance][int accountNum]: Bank/AuctionHouse
     *   Request total balance. Bank responds with balance.
     * - [availableBalance][]: Bank/Agent
     *   Check agent's available balance. Bank responds with balance.
     * - [totalBalance][]: Bank/Agent
     *   Check agent's total balance. Bank responds with balance.
     * - [NewAgent][]: Bank/AuctionHouse to Agent
     *   Establish connection with agent.
     * - [NewAuctionHouse][]: Bank/AuctionHouse
     *   Establish connection with auction house.
     * - [CreateNewAccount][]: Bank/AuctionHouse or Agent
     *   Create new account. Bank responds with account number.
     * - [AuctionAddress][int ip, int port]: Bank/AuctionHouse
     *   Send auction house location to bank.
     * - [BlockFunds][int account, int amount]: Bank/AuctionHouse
     *   Block funds from an agent account.
     * - [UnblockFunds][int account, int amount]: Bank/AuctionHouse
     *   Unblock funds for an agent account.
     * - [AccountNum][int accountNum]: AuctionHouse/Bank
     *   Send account number to auction house upon creation.
     * - [ViewCurrentAuctions][]: Agent/Bank
     *   Request auction info. Bank responds with HashMap<AuctionInfo, Address>.
     * - [ViewCurrentAuctionItems][]: Agent/AuctionHouse
     *   Request auction items. AuctionHouse responds with items collection.
     * - [Bid][String item, int amount, int account]: AuctionHouse/Agent
     *   Send bid info for processing.
     * - [ProcessBlockedFunds][int agentAccount, int auctionAccount]: Bank/AuctionHouse
     *   Transfer blocked funds to auction house after auction ends.
     * - [GetBidStatus][int account]: Agent/AuctionHouse
     *   Request bid status. AuctionHouse sends bid info.
     */

    ArrayList<Object> args = new ArrayList<>();

    public Message(Object... args) {
        for(Object arg : args) {
            this.args.add(arg);
        }
    }

    public String toStrings() {
        String returnStr = new String();
        for(Object arg: args) {
            returnStr += arg.toString();
            returnStr += ",";

        }
        return returnStr;
    }

    public String getCommand() {
        return this.args.get(0).toString();
    }

    public Object splitCommand(int arg) {
        return this.args.get(arg);
    }
}
