package roomescape.controller.response;

import roomescape.domain.ReservationStatus;
import roomescape.service.result.ReservationResult;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationResponse(Long reservationId, //TODO: 이름 고민
                                    String theme,
                                    LocalDate date,
                                    LocalTime time,
                                    ReservationStatus status) {
    public static MyReservationResponse from(ReservationResult reservationResult) {
        return new MyReservationResponse(reservationResult.id(), reservationResult.theme().name(), reservationResult.date(), reservationResult.time().startAt(), reservationResult.status());
    }
}
