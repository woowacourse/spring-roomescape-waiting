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

    public static ReservationDetailResponse from(Reservation reservation, long index) {
        return new ReservationDetailResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt(),
                getStatusNameByIndex(index)
        );
    }

    private static String getStatusName(ReservationStatus status) {
        return switch (status) {
            case BOOKED -> "예약";
            case WAITING -> "%d번째 예약대기";
        };
    }

    private static String getStatusNameByIndex(long index) {
        if (index == 1) {
            return "예약";
        }
        return String.format("%d번째 예약대기", index - 1);
    }
}
