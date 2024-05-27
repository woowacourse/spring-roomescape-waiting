package roomescape.service.response;

import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationWaiting;

public record ReservationWaitingAppResponseWithRank(
        Long id,
        String name,
        ReservationDate date,
        ReservationTimeAppResponse reservationTimeAppResponse,
        ThemeAppResponse themeAppResponse,
        long priority,
        long rank
) {
    public static ReservationWaitingAppResponseWithRank of(ReservationWaiting waiting, long rank) {
        return new ReservationWaitingAppResponseWithRank(
                waiting.getId(),
                waiting.getMember().getName().getName(),
                waiting.getReservation().getReservationDate(),
                ReservationTimeAppResponse.from(waiting.getReservation().getReservationTime()),
                ThemeAppResponse.from(waiting.getReservation().getTheme()),
                waiting.getPriority(),
                rank
        );
    }
}
