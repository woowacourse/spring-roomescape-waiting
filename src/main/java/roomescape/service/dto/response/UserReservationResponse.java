package roomescape.service.dto.response;

import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.WaitingWithRank;

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
        long rank
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

    public static UserReservationResponse from(WaitingWithRank waitingWithRank) {
        return new UserReservationResponse(
                waitingWithRank.waiting().getId(),
                waitingWithRank.waiting().getReservation().getTheme().getName(),
                waitingWithRank.waiting().getReservation().getDate(),
                waitingWithRank.waiting().getReservation().getTime().getStartAt(),
                WAITING.name(),
                waitingWithRank.rank() + 1
        );
    }
}
