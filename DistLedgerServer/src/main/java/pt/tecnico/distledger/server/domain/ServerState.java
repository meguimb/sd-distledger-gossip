package pt.tecnico.distledger.server.domain;

import java.util.*;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.Account;


public class ServerState {

    /*
	 * A lista de operações de escrita aceites, também chamada ledger. Inclui operações 
	 * de 3 tipos: criar conta, remover conta, transferir moeda entre contas. Inicialmente 
	 * está vazia e vai crescendo à medida que lhe são acrescentadas novas operações. 
	 * Um mapa de contas com informação sobre as contas ativas neste momento e o respetivo 
	 * saldo. Esta estrutura descreve o estado que resulta da execução ordenada de todas as 
	 * operações atualmente na ledger e que, na 3ª parte, já estão estáveis (stable updates, 
	 * segundo a terminologia do gossip architecture, o modelo de replicação que vamos usar 
	 * nessa parte do projeto). Sempre que uma nova operação é adicionada à ledger e estabiliza
	 *  (novamente, isto só é relevante na 3ª parte), o estado do mapa de contas deve ser 
	 * atualizado para refletir essa operação.
	 */

	private List<Operation> ledger;
	private Map<String, Account> accountsMap;
    private Boolean is_active;

    public ServerState() {
        // initialize server attributes and lists
        this.ledger = new ArrayList<>();
        this.accountsMap = new HashMap<String, Account>();
        is_active = true;
        // create broker account
        Account broker = new Account("broker");
        broker.setBalance(1000);
        accountsMap.put("broker", broker);
    }
    /* TODO: Here should be declared all the server state attributes
         as well as the methods to access and interact with the state. */

    public int balance(String id) {
        Account account;
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
        Account newAccount = new Account(id);
        // TODO: catch errors
        return addAccount(newAccount);
    }

    public int deleteAccount(String id){
        if (accountsMap.remove(id) == null){
            return -1;
        }
        return 0;
    }

    public int transferTo(String from_id, String to_id, int amount){
        Account from = getAccountsMap().get(from_id);
        Account to = getAccountsMap().get(to_id);
        return from.transferTo(to, amount);
    }

}
