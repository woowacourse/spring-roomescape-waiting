package roomescape.reservation.ui.reservation.display;

import roomescape.common.exception.RoomescapeException;
import roomescape.reservation.domain.reservation.ReservationStatus;

public class ReservationStatusDisplay {

    public static String display(ReservationStatus status) {
        if (status != ReservationStatus.BOOKED) {
            throw new RoomescapeException(
                    String.format("display(ReservationStatus)는 BOOKED 상태만 허용됩니다. [요청한 상태: %s]", status)
            );
        }
        return "예약";
    }

    public static String display(ReservationStatus status, long waitingRank) {
        if (status != ReservationStatus.WAITING) {
            throw new RoomescapeException(
                    String.format("display(ReservationStatus, long)는 WAITING 상태만 허용됩니다. [요청한 상태: %s]", status)
            );
        }
        return String.format("%d번째 예약대기", waitingRank);
    }
}
