package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

public record ReservationDetailResponse(
        long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static ReservationDetailResponse from(Reservation reservation, ReservationStatus status) {
        return new ReservationDetailResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt(),
                getStatusName(status)
        );
    }

    private static String getStatusName(ReservationStatus status) {
        return switch (status) {
            case BOOKED -> "예약";
        };
    }
}
