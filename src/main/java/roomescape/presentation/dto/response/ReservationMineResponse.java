package roomescape.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.vo.Status;

public record ReservationMineResponse(
        String id,
        String themeName,
        LocalDate date,
        LocalTime time,
        Status status,
        Long aheadCount
) {
    public static ReservationMineResponse from(ReservationWithAheadDto myReservationWithAheadDto) {
        Reservation reservation = myReservationWithAheadDto.reservation();
        return new ReservationMineResponse(
                reservation.getId().value(),
                reservation.getTheme().getName().value(),
                reservation.getDate().value(),
                reservation.getTime().getStartTime().value(),
                reservation.getStatus(),
                myReservationWithAheadDto.aheadCount()
        );
    }

    public static List<ReservationMineResponse> from(List<ReservationWithAheadDto> myReservationsWithAheads) {
        return myReservationsWithAheads.stream()
                .map(ReservationMineResponse::from)
                .toList();
    }
}
