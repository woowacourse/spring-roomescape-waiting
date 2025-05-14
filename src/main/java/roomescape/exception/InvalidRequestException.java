package roomescape.exception;

public class InvalidRequestException extends BadRequestException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
