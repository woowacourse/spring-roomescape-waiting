package roomescape.exception;

public class ResourceInUseException extends RoomescapeException {

    public ResourceInUseException(String detail) {
        super(ErrorCode.RESOURCE_IN_USE, detail);
    }
}
