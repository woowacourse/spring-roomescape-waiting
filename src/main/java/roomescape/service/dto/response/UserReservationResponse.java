package roomescape.service.dto.response;

import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;

import static roomescape.domain.reservation.ReservationStatus.RESERVED;
import static roomescape.domain.reservation.ReservationStatus.WAITING;

public record UserReservationResponse(
        long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status,
        int rank
) {
    public static UserReservationResponse reserved(Reservation reservation) {
        return new UserReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                RESERVED.name(),
                Reservation.RESERVATION_RANK
        );
    }

    public static UserReservationResponse from(Waiting waiting, int rank) {
        return new UserReservationResponse(
                waiting.getId(),
                waiting.getReservation().getTheme().getName(),
                waiting.getReservation().getDate(),
                waiting.getReservation().getTime().getStartAt(),
                WAITING.name(),
                rank + 1
        );
    }
}
