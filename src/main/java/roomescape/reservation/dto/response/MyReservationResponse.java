package roomescape.reservation.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingStatus;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        String date,
        String time,
        WaitingStatus status,
        Long waitingRank
) {
    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate().toString(),
                reservation.getReservationTime().toString(),
                WaitingStatus.CONFIRMED,
                null
        );
    }

    public static MyReservationResponse fromWaiting(Waiting waiting, long rank) {
        return new MyReservationResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate().toString(),
                waiting.getTime().getStartAt().toString(),
                WaitingStatus.PENDING,
                rank
        );
    }
}
