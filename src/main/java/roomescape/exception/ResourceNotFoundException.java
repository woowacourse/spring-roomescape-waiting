package roomescape.exception;

public class ResourceNotFoundException extends RoomescapeBaseException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + "을(를) 찾을 수 없습니다. id=" + id);
    }
}