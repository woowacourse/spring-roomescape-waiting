package roomescape.exception.reservationwaiting;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class CannotDeleteOtherMemberWaiting extends RoomescapeException {
    public CannotDeleteOtherMemberWaiting() {
        super("다른 사용자의 예약 대기는 삭제할 수 없습니다.", HttpStatus.FORBIDDEN);
    }
}
