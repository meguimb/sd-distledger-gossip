package pt.tecnico.distledger.server.domain;

import java.util.*;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.Account;


public class ServerState {

	private List<Operation> ledger;
	private Map<String, Account> accountsMap;
    private Boolean is_active;

    public ServerState() {
        this.ledger = new ArrayList<>();
        this.accountsMap = new HashMap<String, Account>();
        is_active = true;
        // add broker user
        Account broker = new Account("broker");
        broker.setBalance(1000);
        addAccount(broker);
    }
    /* TODO: Here should be declared all the server state attributes
         as well as the methods to access and interact with the state. */

    public int balance(String id) {
        Account account;

        if(is_active == false)
            return -2;
        // grab Account by String idS
        account = accountsMap.get(id);
        // check for errors
        if (account == null){
            // add specific exceptions
            return -1;
        }
        return account.getBalance();
    }

    public Map<String, Account> getAccountsMap(){
        return accountsMap;
    }

    public int addAccount(Account a){
        // if returned value of put is different than null is because key already exists
        if (accountsMap.put(a.getName(), a) != null){
            return -1;
        }
        return 0;
    }

    public List<Operation> getLedger(){
        return ledger;
    }

    public void addOperation(Operation o){
        ledger.add(o);
    }
    
    // activate -- coloca o servidor em modo ATIVO (este é o comportamento por omissão), em que responde a todos os pedidos
    public void activate(){
        is_active = true;
    }

    public void deactivate(){
        is_active = false;
    }

    public List<Operation> getLedgerState(){
        return getLedger();
    }

    public void gossip(){
        // TODO - fase 3
    }

    public int createAddAccount(String id){
        if(is_active == false)
            return -1; 

        Account newAccount = new Account(id);
        // TODO: catch errors
        if (addAccount(newAccount) != -1){
            addOperation(new CreateOp(id));
            return 0;
        }
        return -1;
    }

    public int deleteAccount(String id){
        if(is_active == false)
            return -2; 
            
        if (accountsMap.remove(id) == null){
            return -1;
        }
        addOperation(new DeleteOp(id));
        return 0;
    }

    public int transferTo(String from_id, String to_id, int amount){
        Account from = getAccountsMap().get(from_id);
        Account to = getAccountsMap().get(to_id);
        if (from.transferTo(to, amount) != -1){
            addOperation(new TransferOp(from.getName(), to.getName(), amount));
            return 0;
        }
        return -1;
    }

}
