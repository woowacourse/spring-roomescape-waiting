package roomescape.member.presentation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.reservation.domain.Reservation;

public record MyReservationResponse(Long reservationId, String theme, LocalDate date, LocalTime time, String status) {

    public static final String RESERVED = "예약";
    public static final String WAITING = "%d번째 예약대기";

    public static MyReservationResponse from(final Reservation reservation) {
        return new MyReservationResponse(
            reservation.getId(),
            reservation.getTheme().getName(),
            reservation.getDate(),
            reservation.getTime().getStartAt(),
            RESERVED
        );
    }

    public static MyReservationResponse from(final WaitingWithRank waitingWithRank) {
        Reservation reservation = waitingWithRank.getWaiting().getReservation();
        return new MyReservationResponse(
            reservation.getId(),
            reservation.getTheme().getName(),
            reservation.getDate(),
            reservation.getTime().getStartAt(),
            formatRank(waitingWithRank.getRank())
        );
    }

    private static String formatRank(Long rank) {
        return String.format(WAITING, rank);
    }
}
