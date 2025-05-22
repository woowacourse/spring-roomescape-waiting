package roomescape.exception;

public class DuplicateContentException extends BadRequestException {
    public DuplicateContentException(String message) {
        super(message);
    }
}
