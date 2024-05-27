package roomescape.domain.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.WaitingStatus;
import roomescape.domain.WaitingWithRank;

public record ReservationsMineResponse(Long id, String theme, LocalDate date, LocalTime time,
                                       String status) {
    public static ReservationsMineResponse from(Reservation reservation) {
        return new ReservationsMineResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus().getMessage()
        );
    }

    public static ReservationsMineResponse from(WaitingWithRank waiting) {
        return new ReservationsMineResponse(
                waiting.getWaiting().getId(),
                waiting.getWaiting().getTheme().getName(),
                waiting.getWaiting().getDate(),
                waiting.getWaiting().getTime().getStartAt(),
                getStatusMessage(waiting.getWaiting().getStatus(), waiting.getRank()));
    }

    private static String getStatusMessage(WaitingStatus status, Long rank) {
        if (status.doesNotRejected()) {
            return String.format("%s번째 %s", rank, status.getMessage());
        }
        return status.getMessage();
    }
}
