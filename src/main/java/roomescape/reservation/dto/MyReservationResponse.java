package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    private final static long WAITING_ORDER_VALUE = 0;

    public static MyReservationResponse from(final Reservation reservation, final long order) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                formatOrder(reservation.getStatus().getValue(), order)
        );
    }

    private static String formatOrder(final String status, final long order) {
        if (order == WAITING_ORDER_VALUE) {
            return status;
        }
        return String.format("%d번째 %s", order, status);
    }
}
