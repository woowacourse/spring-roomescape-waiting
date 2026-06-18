package roomescape.repository;

public class PersistenceConflictException extends RuntimeException {

    public PersistenceConflictException(final Throwable cause) {
        super(cause);
    }
}
