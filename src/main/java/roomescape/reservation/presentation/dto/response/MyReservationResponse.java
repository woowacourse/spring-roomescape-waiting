package roomescape.reservation.presentation.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.Waiting;

public record MyReservationResponse(Long reservationId,
                                    String theme,
                                    String date,
                                    String time,
                                    String status) {

    public static MyReservationResponse from(final Waiting waiting) {
        Reservation reservation = waiting.getReservation();
        String waitingMessage = String.format(waiting.getWaitingStatus().getTitle(), reservation.findRank(waiting));
        return new MyReservationResponse(reservation.getId(), reservation.getTheme().getName(),
                reservation.getDate().toString(),
                reservation.getTime().getStartAt().toString(), waitingMessage);
    }
}
