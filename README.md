# Auctions — Distributed System

A distributed auction system in Java where a central Bank, multiple
Auction Houses, and multiple bidding Agents communicate over TCP
sockets using serialized message passing.

## Architecture
Bank (fixed address, port)

├── manages accounts for all Agents and Auction Houses

├── tracks balances and blocked funds

└── maintains registry of active Auction Houses

Auction House (dynamic address, registers with Bank)

├── generates items for auction

├── accepts Agent connections

└── validates and processes bids

Agent (client)

├── connects to Bank to create an account

├── browses all registered Auction Houses

└── connects directly to an Auction House to place bids

## Project Structure
src/

Bank/

Bank.java            — Central server entry point

Account.java          — Account balance and fund-blocking logic

AgentHandler.java       — Handles agent connections on the bank

AuctionHandler.java      — Handles auction house connections on the bank

AuctionInfo.java          — Serializable auction house metadata

AuctionHouse/

AuctionHouse.java        — Auction house server entry point

AgentHandler.java         — Handles agent connections on the auction house

Auction.java                — Single item auction with bid logic

Item.java                     — Serializable auction item

Agent/

Agent.java                    — Agent entry point

BankConnection.java            — Manages the agent's connection to the bank

AuctionManager.java             — Manages connections to auction houses

AuctionListener.java             — Background thread for async notifications

AutoBidder.java                   — Automatic incremental bidding

UserInterface.java                 — Console menu

constants/

Message.java                        — Generic serializable message wrapper

AuctionHouseAddress.java             — Serializable network address

doc/

Design Project 5.pdf

## How to Run

Each component runs as a separate process. Open three terminals.

**1. Start the Bank:**
java -jar Bank.jar <bank-port>

**2. Start one or more Auction Houses:**
java -jar AuctionHouse.jar <bank-host> <bank-port>

**3. Start one or more Agents:**
java -jar Agent.jar <bank-host> <bank-port> <agent-name> <initial-funds>

Example:
java -jar Bank.jar 5000

java -jar AuctionHouse.jar localhost 5000

java -jar Agent.jar localhost 5000 Enrique 1000

## How to Use

1. From the Agent menu, select **2** to view all available auction houses
   and their items
2. Select **3** to place a bid — enter the auction house's address
   (shown in step 1), the item name, and your bid amount
3. The agent automatically connects to the auction house if not already
   connected, and receives a live confirmation when the bid is accepted
4. Select **4** to set an automatic maximum bid for an item

## Message Protocol

Communication uses a generic `Message` object carrying a command string
followed by any number of serializable arguments.

**Agent <-> Bank:**
`NewAgent`, `CreateNewAccount`, `ViewCurrentAuctions`, `Terminates`

**Auction House <-> Bank:**
`NewAuctionHouse`, `Create New Account`, `Auction Address`, `Block Funds`

**Agent <-> Auction House:**
`RegisterAgent`, `PlaceBid`, `BidAccepted`, `BidRejected`
