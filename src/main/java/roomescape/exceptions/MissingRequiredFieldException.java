package roomescape.exceptions;

public class MissingRequiredFieldException extends ValidationException {

    public MissingRequiredFieldException(String message) {
        super(message);
    }
}
