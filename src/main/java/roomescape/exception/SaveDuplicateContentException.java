package roomescape.exception;

public class SaveDuplicateContentException extends ConflictException {

    public SaveDuplicateContentException(String message) {
        super(message);
    }

}
