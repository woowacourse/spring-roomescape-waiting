package roomescape.presentation.dto.response;

import roomescape.business.model.entity.Reservation;
import roomescape.business.model.vo.Status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReservationMineResponse(
        String id,
        String themeName,
        LocalDate date,
        LocalTime time,
        Status status,
        Long aheadCount
) {
    public static ReservationMineResponse from(ReservationWithAhead myReservationWithAhead) {
        Reservation reservation = myReservationWithAhead.reservation();
        return new ReservationMineResponse(
                reservation.getId().value(),
                reservation.getTheme().getName().value(),
                reservation.getDate().value(),
                reservation.getTime().getStartTime().value(),
                reservation.getStatus(),
                myReservationWithAhead.aheadCount()
        );
    }

    public static List<ReservationMineResponse> from(List<ReservationWithAhead> myReservationsWithAheads) {
        return myReservationsWithAheads.stream()
                .map(ReservationMineResponse::from)
                .toList();
    }
}
