package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.waiting.domain.WaitingWithRank;

public record MyReservationResponse(
        long id,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        ReservationStatusResponse status
) {

    public MyReservationResponse(final Reservation reservation) {
        this(reservation.getId(),
                reservation.getTheme().getName().getValue(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                new ReservationStatusResponse(ReservationStatus.RESERVED, 0L));
    }

    public MyReservationResponse(final WaitingWithRank waitingWithRank) {
        this(waitingWithRank.getWaiting().getReservation().getId(),
                waitingWithRank.getWaiting().getReservation().getTheme().getName().getValue(),
                waitingWithRank.getWaiting().getReservation().getDate(),
                waitingWithRank.getWaiting().getReservation().getTime().getStartAt(),
                new ReservationStatusResponse(ReservationStatus.WAITING, waitingWithRank.getRank()));
    }
}
