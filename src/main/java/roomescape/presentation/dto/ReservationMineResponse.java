package roomescape.presentation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationMineResponse(
        Long reservationId, String theme, LocalDate date, LocalTime time, String status, Long waitInfoId) {

    public ReservationMineResponse(
            Long reservationId, String theme, LocalDate date, LocalTime time, Long rank, Long waitInfoId) {
        this(reservationId, theme, date, time, formatStatus(rank), waitInfoId);
    }

    private static String formatStatus(final Long rank) {
        if (rank == 1) {
            return "예약";
        }
        return "%d번째 예약대기".formatted(rank);
    }
}
