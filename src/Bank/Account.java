package Bank;
import java.util.HashMap;

public class Account {
    private final int MAX_NUMBER = 10000;
    private int money;
    private int accountNum;
    private int blockFunds;
    String name;

    public Account(int money, HashMap<Integer, Account> allAccounts) {
        this.money = money;
        setUniqueID(allAccounts);
    }


    public void depositMoney(int money) {
        this.money += money;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBlockFunds(int blockFunds) {
        this.blockFunds += blockFunds;
        if(blockFunds < 0) {
            this.blockFunds = 0;
        }
    }

    public void setUnBlockFunds(int unBlockFunds) {
        this.blockFunds -= unBlockFunds;
        if(blockFunds < 0) {
            this.blockFunds = 0;
        }
    }

    public int getMoney() {
        return money;
    }

    public int getAccountNum() {
        return accountNum;
    }

    public int getHeld() {
        return this.blockFunds;
    }

    private void setUniqueID(HashMap<Integer, Account> allAccounts) {
        int idNum;

        do idNum = (int)(Math.random()* MAX_NUMBER);
        while(allAccounts.containsKey(idNum));

        this.accountNum = idNum;
    }

    public void setNewBalance() {
        this.money -= this.blockFunds;
        this.blockFunds = 0;
    }

    public int getUsableMoney() {
        return (this.money - this.blockFunds);
    }
}
