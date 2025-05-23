package roomescape.reservation.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Waiting;

public record MyReservationResponse(Long reservationId,
                                    String theme,
                                    String date,
                                    String time,
                                    String ReservedStatus) {

    public static MyReservationResponse from(final Waiting waiting) {
        return new MyReservationResponse(waiting.getId(), waiting.getInfo().getTheme().getName(),
                waiting.getInfo().getDate().toString(),
                waiting.getInfo().getTime().getStartAt().toString(),
                waiting.getTurn() + "번쩨 " + ReservationStatus.WAITING.getName());
    }


    public static MyReservationResponse from(final Reservation reservation) {
        return new MyReservationResponse(reservation.getId(), reservation.getInfo().getTheme().getName(),
                reservation.getInfo().getDate().toString(),
                reservation.getInfo().getTime().getStartAt().toString(),
                ReservationStatus.RESERVED.getName());
    }
}
