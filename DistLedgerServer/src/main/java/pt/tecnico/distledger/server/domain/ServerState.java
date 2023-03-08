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
    }
    /* TODO: Here should be declared all the server state attributes
         as well as the methods to access and interact with the state. */

    public int balance(String id) {
        Account account;
        // grab Account by String id
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

    public void addAccount(Account a){
        accountsMap.put(a.getName(), a);
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

}
