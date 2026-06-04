package roomescape.exception;

public class ResourceInUseException extends RoomescapeException {

    public ResourceInUseException(String resourceName) {
        super("RESOURCE_IN_USE", "해당 " + resourceName + "를(을) 참조하고 있는 데이터가 있어 삭제할 수 없습니다.");
    }
}
