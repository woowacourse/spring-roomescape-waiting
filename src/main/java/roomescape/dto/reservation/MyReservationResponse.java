package roomescape.dto.reservation;

import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingWithRank;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        String time,
        String status
) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String RESERVED = "예약";
    private static final String WAITING_WITH_RANK = "%d번째 예약대기";

    public static MyReservationResponse from(final Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt().format(FORMATTER),
                RESERVED
        );
    }

    public static MyReservationResponse from(final WaitingWithRank waitingWithRank) {
        Waiting waiting = waitingWithRank.getWaiting();
        return new MyReservationResponse(
                waiting.getId(),
                waiting.getThemeName(),
                waiting.getDate(),
                waiting.getStartAt().format(FORMATTER),
                String.format(WAITING_WITH_RANK, waitingWithRank.getRank())
        );
    }
}
