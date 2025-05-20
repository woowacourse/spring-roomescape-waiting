package roomescape.presentation.dto.response;

import roomescape.business.dto.ReservationDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.business.model.vo.Status;

public record ReservationMineResponse(
        String id,
        String themeName,
        LocalDate date,
        LocalTime time,
        Status status
) {
    public static ReservationMineResponse from(ReservationDto myReservations) {
        return new ReservationMineResponse(
                myReservations.id().value(),
                myReservations.theme().name().value(),
                myReservations.date().value(),
                myReservations.time().startTime().value(),
                myReservations.status()
        );
    }

    public static List<ReservationMineResponse> from(List<ReservationDto> myReservations) {
        return myReservations.stream()
                .map(ReservationMineResponse::from)
                .toList();
    }
}
