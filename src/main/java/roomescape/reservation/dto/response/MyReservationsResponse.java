package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.WaitingWithRank;

public record MyReservationsResponse(
        Long id,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {

    public static MyReservationsResponse from(final Reservation reservation) {
        return new MyReservationsResponse(
                reservation.getId(),
                reservation.themeName(),
                reservation.getDate(),
                reservation.startTime(),
                reservation.statusDescription()
        );
    }

    public static MyReservationsResponse from(final WaitingWithRank waitingWithRank) {
        return new MyReservationsResponse(
                waitingWithRank.getId(),
                waitingWithRank.themeName(),
                waitingWithRank.getDate(),
                waitingWithRank.startTime(),
                String.valueOf(waitingWithRank.getRank())
        );
    }
}
