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
    static int timestampIndex;
    public static List<Integer> TS;
    private Boolean is_active = true;
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    public ServerState() {
        TS = new ArrayList<Integer>();
        TS.add(0);
        TS.add(0);
        TS.add(0);
        this.ledger = new ArrayList<>();
        this.accountsMap = new HashMap<String, Account>();
        
        // add broker user
        Account broker = new Account("broker");
        broker.setBalance(1000);
        addAccount(broker);
    }

    public List<Integer> getTS() {
        return TS;
    }

    public void updateTS() {
        TS.set(timestampIndex, TS.get(timestampIndex) + 1);
    }

    public void setTimestampIndex(int index) {
        timestampIndex = index;
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
    public int createAddAccount(String id, Boolean isPropagating, List<Integer> TS, List<Integer> PrevTS, Boolean stable, Boolean existingOperation) throws ServerNotActiveException, SecondaryServerException, AccountAlreadyExistsException {
        // if server is not active, don't perform operation
        if(is_active == false)
            throw new ServerNotActiveException();

        if (!existingOperation)
            addOperation(new CreateOp(id, TS, PrevTS, stable));
        if(stable) {
            Account newAccount = new Account(id);
            addAccount(newAccount);
            return 0;
        }
        else if(!stable) {
            return 0;
        }

        throw new AccountAlreadyExistsException();
    }

    // operação de escrita
    /*public int deleteAccount(String id, Boolean isPropagating) throws ServerNotActiveException, SecondaryServerException {

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
    }*/

    // operação de escrita 
    public int transferTo(String from_id, String to_id, int amount, Boolean isPropagating, List<Integer> TS, List<Integer> PrevTS, Boolean stable, Boolean existingOperation) throws ServerNotActiveException, SecondaryServerException, 
    TransferToAndFromSameAccountException, AccountDoesNotExistException, InvalidAmountException, InvalidTransferOperationException {

        // if server's not active, don't perform operation
        if (is_active == false && stable){
            throw new ServerNotActiveException();
        }

        // check if trying to transfer to and from the same account
        if (from_id.equals(to_id) && stable){
            throw new TransferToAndFromSameAccountException(to_id);
        }

        // check if amount to transfer is valid
        if (amount <= 0 && stable){
            throw new InvalidAmountException(amount);
        }

        // check if source account is valid
        Account from = getAccountsMap().get(from_id);
        if (from == null && stable){
            throw new AccountDoesNotExistException(from_id);
        }

        // check if dest account is valid
        Account to = getAccountsMap().get(to_id);
        if (to == null && stable){
            throw new AccountDoesNotExistException(to_id);
        }

        // do transferTo operation
        synchronized (this){   
            // if transfer operation is valid, add it to ledger
            if(!stable){
                if (!existingOperation)
                    addOperation(new TransferOp(from_id, to_id, amount, TS, PrevTS, stable));
                
                return 0;
            }
            else if (from.transferTo(to, amount) != -1 && stable){
                if (!existingOperation)
                    addOperation(new TransferOp(from.getName(), to.getName(), amount, TS, PrevTS, stable));
                return 0;
            }
            throw new InvalidTransferOperationException();
        }
    }

    public int propagateState(List<Operation> newLedger, List<Integer> replicaTS) throws SecondaryServerNotActiveException {
        if (is_active == false)
            throw new SecondaryServerNotActiveException();

        if (TS.get(0) <= replicaTS.get(0) && TS.get(1) <= replicaTS.get(1) && TS.get(2) <= replicaTS.get(2)) {
            // update server timestamp
            TS.set(0, replicaTS.get(0));
            TS.set(1, replicaTS.get(1));
            TS.set(2, replicaTS.get(2));
        }

        Operation newLedgerOp;
        Operation op;

        // add to ledger and if stable, do operation
        Integer size = ledger.size();
        Integer newSize = newLedger.size();
        for (int i = 0; i < newSize; i++) {
            newLedgerOp = newLedger.get(i);
            for (int j = 0; j < size; j++) {
                op = ledger.get(j);

                // check if operation is already in ledger
                if (newLedgerOp instanceof CreateOp && op instanceof CreateOp) {
                    CreateOp newCreateOp = (CreateOp) newLedgerOp;
                    CreateOp createOp = (CreateOp) op;
                    if (newCreateOp.getAccount().equals(createOp.getAccount()) && newCreateOp.getPrevTS().equals(createOp.getPrevTS())) {
                        break;
                    }
                }
                else if (newLedgerOp instanceof TransferOp && op instanceof TransferOp) {
                    TransferOp newTransferOp = (TransferOp) newLedgerOp;
                    TransferOp transferOp = (TransferOp) op;
                    if (newTransferOp.getAccount().equals(transferOp.getAccount()) && newTransferOp.getDestAccount().equals(transferOp.getDestAccount()) && newTransferOp.getAmount() == transferOp.getAmount() && newTransferOp.getPrevTS().equals(transferOp.getPrevTS())) {
                        break;
                    }
                }

                // add operation to ledger if it's not already there
                if (j == size - 1) {
                    Boolean stable = true;
                    if (TS.get(0) < newLedgerOp.getPrevTS().get(0) || TS.get(1) < newLedgerOp.getPrevTS().get(1) || TS.get(2) < newLedgerOp.getPrevTS().get(2)) {
                        stable = false;
                    }

                    // do operation and add it to ledger
                    if (newLedgerOp instanceof TransferOp){
                        TransferOp transferOp = (TransferOp) newLedgerOp;
                        try {
                            transferTo(transferOp.getAccount(), transferOp.getDestAccount(), transferOp.getAmount(), true, transferOp.getTS(), transferOp.getPrevTS(), stable, false);
                        } catch (Exception e) {
                            throw new SecondaryServerNotActiveException();
                        }
                    }
                    else if (newLedgerOp instanceof CreateOp){
                        CreateOp createOp = (CreateOp) newLedgerOp;
                        try {
                            createAddAccount(createOp.getAccount(), true, createOp.getTS(), createOp.getPrevTS(), stable, false);
                        } catch (Exception e) {
                            throw new SecondaryServerNotActiveException();
                        }
                    }
                }
            }

            //if current ledger is empty add all operations
            if(size == 0) {
                Boolean stable = true;
                if (TS.get(0) < newLedgerOp.getPrevTS().get(0) || TS.get(1) < newLedgerOp.getPrevTS().get(1) || TS.get(2) < newLedgerOp.getPrevTS().get(2)) {
                    stable = false;
                }

                // do operation and add it to ledger
                if (newLedgerOp instanceof TransferOp){
                    TransferOp transferOp = (TransferOp) newLedgerOp;
                    try {
                        transferTo(transferOp.getAccount(), transferOp.getDestAccount(), transferOp.getAmount(), true, transferOp.getTS(), transferOp.getPrevTS(), stable, false);
                    } catch (Exception e) {
                        throw new SecondaryServerNotActiveException();
                    }
                }
                else if (newLedgerOp instanceof CreateOp){
                    CreateOp createOp = (CreateOp) newLedgerOp;
                    try {
                        createAddAccount(createOp.getAccount(), true, createOp.getTS(), createOp.getPrevTS(), stable, false);
                    } catch (Exception e) {
                        throw new SecondaryServerNotActiveException();
                    }
                }
            }
        }
        // look through ledger and do unstable operations that are now stable
        for (int i = 0; i < ledger.size(); i++) {
            op = ledger.get(i);
            if (!op.isStable()) {
                if (TS.get(0) >= op.getPrevTS().get(0) && TS.get(1) >= op.getPrevTS().get(1) && TS.get(2) >= op.getPrevTS().get(2)) {
                    if (op instanceof TransferOp){
                        TransferOp transferOp = (TransferOp) op;
                        try {
                            transferOp.setStable();
                            transferTo(transferOp.getAccount(), transferOp.getDestAccount(), transferOp.getAmount(), true, transferOp.getTS(), transferOp.getPrevTS(), transferOp.isStable(), true);
                        } catch (Exception e) {
                            throw new SecondaryServerNotActiveException();
                        }
                    }
                    else if (op instanceof CreateOp){
                        CreateOp createOp = (CreateOp) op;
                        try {
                            createOp.setStable();
                            createAddAccount(createOp.getAccount(), true, createOp.getTS(), createOp.getPrevTS(), createOp.isStable(), true);
                        } catch (Exception e) {
                            throw new SecondaryServerNotActiveException();
                        }
                    }
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
