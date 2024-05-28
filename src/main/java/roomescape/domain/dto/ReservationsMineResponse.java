package roomescape.domain.dto;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationsMineResponse(Long id, String theme, LocalDate date, LocalTime time,
                                       String status) {
    public static final String WAITING_FORMAT = "%s번째 예약대기";

    public static ReservationsMineResponse from(Reservation reservation, Integer rank) {
        return new ReservationsMineResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                buildStatusMessage(reservation.getStatus(), rank)
        );
    }

    private static String buildStatusMessage(final ReservationStatus status, Integer rank) {
        if (status.isWaiting()) {
            return WAITING_FORMAT.formatted(rank);
        }
        return status.getMessage();
    }
}
