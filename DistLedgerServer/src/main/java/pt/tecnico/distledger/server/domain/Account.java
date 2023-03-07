package pt.tecnico.distledger.server.domain.operation;

public class User {
    private String name;
    private int balance;

    public User(String name){
        setName(name);
        setBalance(0);
    }

    public Boolean equal(User u){
        return this.getName() == u.getName();
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getBalance(){
        return this.balance;
    }

    public void setBalance(int balance){
        this.balance = balance;
    }

    public void deposit(int amount){
        setBalance(getBalance() + amount);
    }

    public void withdraw(int amount){
        setBalance(getBalance() - amount);
    }

    public int transferTo(User u, int amount){
        if (this.equals(u)){
            return -1;
        }
        else if(amount <= 0 || amount > this.balance){
            return -1;
        }
        else{
            u.deposit(amount);
            this.withdraw(amount);
        }
        return 1;

    }

}
