package roomescape.exception;

public class DuplicatedException extends RuntimeException {

    public DuplicatedException(String message) {
        super("[ERROR] " + message);
    }
}
