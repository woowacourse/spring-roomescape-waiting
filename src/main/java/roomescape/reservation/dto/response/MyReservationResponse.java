package roomescape.reservation.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.Waiting;

public record MyReservationResponse(
        Long id,
        String theme,
        String date,
        String time,
        String status
) {
    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate().toString(),
                reservation.getReservationTime().toString(),
                "예약"
        );
    }

    public static MyReservationResponse fromWaiting(Waiting waiting, long rank) {
        return new MyReservationResponse(
                waiting.getId(),
                waiting.getTheme().getName(),
                waiting.getDate().toString(),
                waiting.getTime().getStartAt().toString(),
                "대기 " + rank + "번째"
        );
    }
}
