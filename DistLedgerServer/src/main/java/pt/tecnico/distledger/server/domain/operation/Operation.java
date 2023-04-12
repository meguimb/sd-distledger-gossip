package pt.tecnico.distledger.server.domain.operation;

import java.util.List;

public class Operation {
    private String account;
    private List<Integer> TS;
    private List<Integer> prevTS;
    private Boolean stable;

    public Operation(String fromAccount, List<Integer> TS, List<Integer> PrevTS, Boolean stable) {
        this.account = fromAccount;
        this.stable = stable;
        this.TS = TS;
        this.prevTS = PrevTS;
    }

    public String getAccount() {
        return account;
    }

    public List<Integer> getTS() {
        return TS;
    }

    public List<Integer> getPrevTS() {
        return prevTS;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setUnstable() {
        this.stable = false;
    }

    public void setStable() {
        this.stable = true;
    }

    public Boolean isStable() {
        return stable;
    }

}
