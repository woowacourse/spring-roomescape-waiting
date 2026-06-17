package roomescape.reservation.query.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.domain.ReservationWaiting;

public record ReservationWithStatusResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        ReservationStatus status,
        Long waitingOrder,
        String orderId
) {

    public static ReservationWithStatusResult from(Reservation reservation) {
        return from(reservation, null);
    }

    public static ReservationWithStatusResult from(Reservation reservation, String orderId) {
        return new ReservationWithStatusResult(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                ReservationStatus.RESERVED,
                0L,
                orderId
        );
    }

    public static ReservationWithStatusResult from(ReservationWaiting waiting, long rank) {
        return new ReservationWithStatusResult(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme(),
                ReservationStatus.WAITING,
                rank,
                null
        );
    }
}
