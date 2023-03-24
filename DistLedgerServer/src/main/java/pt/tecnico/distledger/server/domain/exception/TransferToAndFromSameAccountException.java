package pt.tecnico.distledger.server.domain.exception;

public class TransferToAndFromSameAccountException extends IllegalArgumentException {
    private final String id;

    public TransferToAndFromSameAccountException(String id) {
        super("You can't transfer to and from the same account!");
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
