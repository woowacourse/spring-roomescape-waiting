package roomescape.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.business.dto.ReservationDto;
import roomescape.business.dto.ReservationTimeDto;
import roomescape.business.model.vo.ReservationStatus;

public record ReservationMineResponse(
        String id,
        String themeName,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static ReservationMineResponse from(ReservationDto myReservations) {
        return new ReservationMineResponse(
                myReservations.id().value(),
                myReservations.theme().name().value(),
                myReservations.date().value(),
                myReservations.time().startTime().value(),
                ReservationStatus.RESERVED.getDisplayName()
        );
    }

    public static List<ReservationMineResponse> from(List<ReservationDto> myReservations) {
        return myReservations.stream()
                .map(ReservationMineResponse::from)
                .toList();
    }
}
