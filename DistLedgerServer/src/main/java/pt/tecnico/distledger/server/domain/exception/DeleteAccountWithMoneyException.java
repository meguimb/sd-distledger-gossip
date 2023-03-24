package pt.tecnico.distledger.server.domain.exception;

public class DeleteAccountWithMoneyException extends IllegalArgumentException {
    public DeleteAccountWithMoneyException() {
        super("You can't delete an account that has money.");
    }
}
