package roomescape.reservation.controller.dto;

import roomescape.reservation.service.dto.ReservationWaitingResult;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.theme.controller.dto.ThemeResponse;

public sealed interface ReservationWaitingResponse permits ConfirmedReservationResponse, WaitingReservationResponse {

    Long id();

    String guestName();

    String date();

    ReservationTimeResponse time();

    ThemeResponse theme();

    String status();

    boolean isConfirmed();

    static ReservationWaitingResponse from(ReservationWaitingResult reservationWaitingResult) {
        if (reservationWaitingResult.status().isWaiting()) {
            return WaitingReservationResponse.from(reservationWaitingResult);
        }
        return ConfirmedReservationResponse.from(reservationWaitingResult);
    }
}
