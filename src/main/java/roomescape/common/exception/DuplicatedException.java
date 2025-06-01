package roomescape.common.exception;

public class DuplicatedException extends IllegalArgumentException {
    public DuplicatedException(String s) {
        super(s);
    }
}
