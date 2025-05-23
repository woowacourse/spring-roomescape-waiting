package roomescape.reservation.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public record MyReservationResponse(Long reservationId,
                                    String theme,
                                    String date,
                                    String time,
                                    String reservedStatus) {

    public static MyReservationResponse from(final WaitingWithRank waitingWithRank) {
        return new MyReservationResponse(waitingWithRank.getWaiting().getId(), waitingWithRank.getWaiting().getInfo().getTheme().getName(),
                waitingWithRank.getWaiting().getInfo().getDate().toString(),
                waitingWithRank.getWaiting().getInfo().getTime().getStartAt().toString(),
                waitingWithRank.getRank() + "번쩨 " + ReservationStatus.WAITING.getName());
    }


    public static MyReservationResponse from(final Reservation reservation) {
        return new MyReservationResponse(reservation.getId(), reservation.getInfo().getTheme().getName(),
                reservation.getInfo().getDate().toString(),
                reservation.getInfo().getTime().getStartAt().toString(),
                ReservationStatus.RESERVED.getName());
    }
}
