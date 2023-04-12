package pt.tecnico.distledger.server.domain.operation;

import java.util.List;

public class DeleteOp extends Operation {

    public DeleteOp(String account, List<Integer> TS, List<Integer> PrevTS, Boolean stable) {
        super(account, TS, PrevTS, stable);
    }

}
