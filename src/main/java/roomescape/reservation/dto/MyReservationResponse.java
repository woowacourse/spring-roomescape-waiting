package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        String status,
        Long rank
) {

    public MyReservationResponse(final Reservation reservation, final ReservationStatus status) {
        this(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                status.getOutput(),
                null
        );
    }

    public MyReservationResponse(final WaitingReservationWithRank waitingReservationWithRank,
                                 ReservationStatus status) {
        this(
                waitingReservationWithRank.reservationId(),
                waitingReservationWithRank.theme(),
                waitingReservationWithRank.date(),
                waitingReservationWithRank.time(),
                status.getOutput(),
                waitingReservationWithRank.rank()
        );
    }

    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(reservation, ReservationStatus.BOOKED);
    }

    public static MyReservationResponse from(WaitingReservationWithRank waiting) {
        return new MyReservationResponse(waiting, ReservationStatus.WAITING);
    }
}
