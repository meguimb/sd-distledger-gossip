package pt.tecnico.distledger.server.domain.exception;

public class AccountDoesNotExistException extends IllegalArgumentException {
    private final String id;

    public AccountDoesNotExistException(String id) {
        super("Account " + id + " does not exist.");
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
