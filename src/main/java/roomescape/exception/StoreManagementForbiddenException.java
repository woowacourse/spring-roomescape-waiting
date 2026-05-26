package roomescape.exception;

public class StoreManagementForbiddenException extends RoomescapeBaseException {
    public StoreManagementForbiddenException() {
        super("본인이 관리하는 매장의 예약만 관리할 수 있습니다.");
    }
}
