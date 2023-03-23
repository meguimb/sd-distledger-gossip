package pt.tecnico.distledger.server.domain;

import java.util.*;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.grpc.DistLedgerService;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.Account;


public class ServerState {

	private List<Operation> ledger;
	private Map<String, Account> accountsMap;
    private Boolean is_active = true;
    private char qualificator;
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    public ServerState(char qualificator) {
        this.ledger = new ArrayList<>();
        this.accountsMap = new HashMap<String, Account>();
        this.qualificator = qualificator;
        
        // add broker user
        Account broker = new Account("broker");
        broker.setBalance(1000);
        addAccount(broker);
    }

    // operação de leitura
    public int balance(String id) {
        Account account;    
        int value;

        // return -2 if server not active
        if(is_active == false)
            return -2;
        
        account = accountsMap.get(id);
        // return -1 if account not active
        if (account == null){
            return -1;
        }
        value = account.getBalance();
        return value;
    }

    public Map<String, Account> getAccountsMap(){
        return accountsMap;
    }

    public int addAccount(Account a){
        // check if account already exists
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
    
    // function to activate the server
    public void activate(){
        is_active = true;
    }

    // function to deactivate the server
    public void deactivate(){
        is_active = false;
    }

    public Boolean getIsActive(){
        return this.is_active;
    }

    // operação de leitura
    public List<Operation> getLedgerState(){
        return getLedger();
    }

    public void gossip(){
        // TODO - fase 3
    }

    // operação de escrita
    public int createAddAccount(String id, Boolean isPropagating){
        if(is_active == false)
            return -2;
        if(qualificator == 'B' && isPropagating == false)
            return -3;

        Account newAccount = new Account(id);

        // check if addOperation is valid and add it to ledger
        if (addAccount(newAccount) != -1){
            addOperation(new CreateOp(id));
            return 0;
        }
        return -1;
    }

    // operação de escrita
    public int deleteAccount(String id, Boolean isPropagating){

        // if server is not active, don't perform operation
        if(is_active == false)
            return -2;
        if(qualificator == 'B' && isPropagating == false)
            return -4;

        // check if account is valid
        Account a = getAccountsMap().get(id);
        if (a == null){
            return -1;
        }

        synchronized (a){

            // check if balance is not 0
            if (a.getBalance() != 0){
                return -3;
            }

            // check if we're able to remove account
            if (accountsMap.remove(id) == null){
                return -1;
            }

            // if delete operation is valid, add it ledger
            addOperation(new DeleteOp(id));
            return 0;
        }
    }

    // operação de escrita 
    public int transferTo(String from_id, String to_id, int amount, Boolean isPropagating){

        // if server's not active, don't perform operation
        if (is_active == false){
            return -2;
        }
        if(qualificator == 'B' && isPropagating == false)
            return -6;

        // check if trying to transfer to and from the same account
        if (from_id.equals(to_id)){
            return -3;
        }

        // check if amount to transfer is valid
        if (amount <= 0){
            return -4;
        }

        // check if source account is valid
        Account from = getAccountsMap().get(from_id);
        if (from == null){
            return -5;
        }

        // check if dest account is valid
        Account to = getAccountsMap().get(to_id);
        if (to == null){
            return -5;
        }

        // do transferTo operation
        synchronized (this){   
            // if transfer operation is valid, add it to ledger
            if (from.transferTo(to, amount) != -1){
                addOperation(new TransferOp(from.getName(), to.getName(), amount));
                return 0;
            }
            return -1;
        }
    }

    public int propagateState(List<Operation> newLedger){
        // reset everything
        List<Operation> temp = newLedger;
        Operation parentOp;

        this.ledger = new ArrayList<>();
        this.accountsMap = new HashMap<String, Account>();
        is_active = true;

        // add broker user
        Account broker = new Account("broker");
        broker.setBalance(1000);
        addAccount(broker);

        // do each operation from state's ledger
        for (int i = 0; i < temp.size(); i++) {
            parentOp = temp.get(i);
            // do operation and add it to ledger
            if (parentOp instanceof TransferOp){
                TransferOp op = (TransferOp) parentOp;
                transferTo(op.getAccount(), op.getDestAccount(), op.getAmount(), true);
            }
            else if (parentOp instanceof CreateOp){
                CreateOp op = (CreateOp) parentOp;
                createAddAccount(op.getAccount(), true);
            }
            else if (parentOp instanceof DeleteOp){
                DeleteOp op = (DeleteOp) parentOp;
                deleteAccount(op.getAccount(), true);
            }
        }
        return 0;
    }

    public void debug(String message) {
        if (DEBUG_FLAG) {
            System.err.println("[DEBUG] " + message);
        }
    }
  
    public void info(String message) {
        System.out.println("[INFO] " + message);
    }
}
