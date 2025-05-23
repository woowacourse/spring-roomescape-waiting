package roomescape.reservation.presentation.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public record MyReservationResponse(Long reservationId,
                                    String theme,
                                    String date,
                                    String time,
                                    String status) {

    // TODO : 수정 필요
    public static MyReservationResponse from(final Reservation reservation) {
        return new MyReservationResponse(reservation.getId(), reservation.getTheme().getName(),
                reservation.getDate().toString(),
                reservation.getTime().getStartAt().toString(), ReservationStatus.RESERVED.getName());
    }
}
