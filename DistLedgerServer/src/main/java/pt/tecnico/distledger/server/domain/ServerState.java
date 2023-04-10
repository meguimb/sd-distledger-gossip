package pt.tecnico.distledger.server.domain;

import java.util.*;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.grpc.DistLedgerService;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.Account;

import pt.tecnico.distledger.server.domain.exception.ServerNotActiveException;
import pt.tecnico.distledger.server.domain.exception.SecondaryServerException;
import pt.tecnico.distledger.server.domain.exception.TransferToAndFromSameAccountException;
import pt.tecnico.distledger.server.domain.exception.InvalidAmountException;
import pt.tecnico.distledger.server.domain.exception.AccountDoesNotExistException;
import pt.tecnico.distledger.server.domain.exception.InvalidTransferOperationException;
import pt.tecnico.distledger.server.domain.exception.DeleteInvalidAccountException;
import pt.tecnico.distledger.server.domain.exception.DeleteAccountWithMoneyException;
import pt.tecnico.distledger.server.domain.exception.AccountAlreadyExistsException;
import pt.tecnico.distledger.server.domain.exception.SecondaryServerNotActiveException;
public class ServerState {

	private List<Operation> ledger;
	private Map<String, Account> accountsMap;
    private Boolean is_active = true;
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    public ServerState() {
        this.ledger = new ArrayList<>();
        this.accountsMap = new HashMap<String, Account>();
        
        // add broker user
        Account broker = new Account("broker");
        broker.setBalance(1000);
        addAccount(broker);
    }

    // operação de leitura
    public int balance(String id) throws ServerNotActiveException {
        Account account;    
        int value;

        // error if server not active
        if(is_active == false)
            throw new ServerNotActiveException();
        
        account = accountsMap.get(id);

        // error if account doesn't exist
        if (account == null)
            throw new AccountDoesNotExistException(id);
        
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
    public int createAddAccount(String id, Boolean isPropagating) throws ServerNotActiveException, SecondaryServerException, AccountAlreadyExistsException {
        // if server is not active, don't perform operation
        if(is_active == false)
            throw new ServerNotActiveException();

        Account newAccount = new Account(id);

        // check if addOperation is valid and add it to ledger
        if (addAccount(newAccount) != -1){
            addOperation(new CreateOp(id));
            return 0;
        }
        throw new AccountAlreadyExistsException();
    }

    // operação de escrita
    public int deleteAccount(String id, Boolean isPropagating) throws ServerNotActiveException, SecondaryServerException {

        // if server is not active, don't perform operation
        if(is_active == false)
            throw new ServerNotActiveException();

        // check if account is valid
        Account a = getAccountsMap().get(id);
        if (a == null){
            throw new DeleteInvalidAccountException();
        }

        synchronized (a){

            // check if balance is not 0
            if (a.getBalance() != 0){
                throw new DeleteAccountWithMoneyException();
            }

            // check if we're able to remove account
            if (accountsMap.remove(id) == null){
                throw new DeleteInvalidAccountException();
            }

            // if delete operation is valid, add it ledger
            addOperation(new DeleteOp(id));
            return 0;
        }
    }

    // operação de escrita 
    public int transferTo(String from_id, String to_id, int amount, Boolean isPropagating) throws ServerNotActiveException, SecondaryServerException, 
    TransferToAndFromSameAccountException, AccountDoesNotExistException, InvalidAmountException, InvalidTransferOperationException {

        // if server's not active, don't perform operation
        if (is_active == false){
            throw new ServerNotActiveException();
        }

        // check if trying to transfer to and from the same account
        if (from_id.equals(to_id)){
            throw new TransferToAndFromSameAccountException(to_id);
        }

        // check if amount to transfer is valid
        if (amount <= 0){
            throw new InvalidAmountException(amount);
        }

        // check if source account is valid
        Account from = getAccountsMap().get(from_id);
        if (from == null){
            throw new AccountDoesNotExistException(from_id);
        }

        // check if dest account is valid
        Account to = getAccountsMap().get(to_id);
        if (to == null){
            throw new AccountDoesNotExistException(to_id);
        }

        // do transferTo operation
        synchronized (this){   
            // if transfer operation is valid, add it to ledger
            if (from.transferTo(to, amount) != -1){
                addOperation(new TransferOp(from.getName(), to.getName(), amount));
                return 0;
            }
            throw new InvalidTransferOperationException();
        }
    }

    public int propagateState(List<Operation> newLedger) throws SecondaryServerNotActiveException {
        if (is_active == false)
            throw new SecondaryServerNotActiveException();

        List<Operation> temp = newLedger;
        Operation parentOp;
        // reset everything
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
                try {
                    transferTo(op.getAccount(), op.getDestAccount(), op.getAmount(), true); 
                } catch (Exception e) {
                    throw new SecondaryServerNotActiveException();
                }
            }
            else if (parentOp instanceof CreateOp){
                CreateOp op = (CreateOp) parentOp;
                try {
                    createAddAccount(op.getAccount(), true);
                } catch (Exception e) {
                    throw new SecondaryServerNotActiveException();
                }
            }
            else if (parentOp instanceof DeleteOp){
                DeleteOp op = (DeleteOp) parentOp;
                try {
                    deleteAccount(op.getAccount(), true);
                } catch (Exception e) {
                    throw new SecondaryServerNotActiveException();
                }
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
