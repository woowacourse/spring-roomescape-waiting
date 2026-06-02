package roomescape.service.dto.result;

import roomescape.dao.dto.TimeQueryResult;

public record ReservationTimeDetailResult(
        ReservationTimeResult timeResult,
        boolean isReservable
) {
    public static ReservationTimeDetailResult from(TimeQueryResult result) {
        return new ReservationTimeDetailResult(
                ReservationTimeResult.from(result.reservationTime()),
                result.isReservable()
        );
    }
}
