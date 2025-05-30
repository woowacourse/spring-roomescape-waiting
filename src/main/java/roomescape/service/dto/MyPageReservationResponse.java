package roomescape.service.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;

public record MyPageReservationResponse(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    public static MyPageReservationResponse from(final Reservation reservation) {
        return new MyPageReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "예약"
        );
    }

    public static MyPageReservationResponse of(final ReservationWaiting reservationWaiting, final long rank) {
        return new MyPageReservationResponse(
                reservationWaiting.getId(),
                reservationWaiting.getTheme().getName(),
                reservationWaiting.getDate(),
                reservationWaiting.getTime().getStartAt(),
                String.format("%d번째 예약대기", rank)
        );
    }
}
