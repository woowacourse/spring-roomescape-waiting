package roomescape.service.dto.result;

import java.time.LocalDate;
import roomescape.dao.dto.WaitingQueryResult;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;

public record ReservationDetailResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResult timeResult,
        ThemeResult themeResult,
        ReservationStatus status,
        Integer sequence
) {

    public static ReservationDetailResult fromReservation(Reservation reservation) {
        return new ReservationDetailResult(
                reservation.getId(),
                reservation.getName().value(),
                reservation.getDate(),
                ReservationTimeResult.from(reservation.getTime()),
                ThemeResult.from(reservation.getTheme()),
                ReservationStatus.RESERVED,
                null
        );
    }

    public static ReservationDetailResult fromWaiting(WaitingQueryResult queryResult) {
        return new ReservationDetailResult(
                queryResult.id(),
                queryResult.name().value(),
                queryResult.date(),
                ReservationTimeResult.from(queryResult.time()),
                ThemeResult.from(queryResult.theme()),
                ReservationStatus.WAITING,
                queryResult.sequence()
        );
    }
}