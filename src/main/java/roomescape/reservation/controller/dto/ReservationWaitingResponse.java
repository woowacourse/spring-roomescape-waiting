package roomescape.reservation.controller.dto;

import roomescape.reservation.service.dto.ReservationWaitingResult;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.theme.controller.dto.ThemeResponse;

public sealed interface ReservationWaitingResponse permits ConfirmedReservationResponse, WaitingReservationResponse, CanceledReservationResponse {
    Long id();
    String guestName();
    String date();
    ReservationTimeResponse time();
    ThemeResponse theme();
    String status();

    static ReservationWaitingResponse from(ReservationWaitingResult reservationWaitingResult) {
        return switch (reservationWaitingResult.status()) {
            case CONFIRMED -> ConfirmedReservationResponse.from(reservationWaitingResult);
            case WAITING -> WaitingReservationResponse.from(reservationWaitingResult);
            case CANCELED -> CanceledReservationResponse.from(reservationWaitingResult);
        };
    }
}
