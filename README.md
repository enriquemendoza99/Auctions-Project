# Project Auctions
This program involves simulating a distributed system with the following components:

Bank: The central entity located at a fixed, known address. It manages the accounts of both auction houses and agents, tracking balances and facilitating transactions.

Auction Houses: Distributed entities responsible for listing items for auction and managing bids. These entities will be created dynamically and operate independently on separate machines.

Agents: Clients dynamically created on other machines. They interact with the auction houses to place bids and purchase items. Agents will communicate with the bank to ensure they have sufficient funds for transactions.
# Implementation Details
1. We chose to do a console version of the auctions project.
2. it's sending messages throw all computer machines showing that they all connect.
3. The Bank and Agents are their own servers, but the auctionHouse is working as a client.

# How to Run 
## Bank.jar:
java -jar Bank.jar <BANK-PORT>
## Agent.jar
java -jar Agent.jar <BANK-HOST> <BANK-PORT> <AGENT-NAME> <INITIAL-FUND>
## AuctionHouse
java -jar AuctionHouse.jar <BANK-HOST> <BANK-PORT>

# Doc contains:
The doc directory contains the object design for our Auctions program.

# Known issues 
1. We weren't able to have the auctionHouse as a server.
2. We couldn't get the biding to work properly.

# How We Divided Work:
## Ricardo Rangel-Valencia
- Bank package
- constants package

## Alexander Leon 
- AuctionHouse

## Enrique Mendoza
- Agent

But we all help out towards the end due to problems we had.