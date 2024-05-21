package roomescape.dto.reservation;

import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        String time,
        String status
) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Map<ReservationStatus, String> STATUS_VALUES = Map.of(
            ReservationStatus.RESERVED, "예약",
            ReservationStatus.WAITING, "대기"
    );

    public static MyReservationResponse from(final Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt().format(FORMATTER),
                STATUS_VALUES.get(reservation.getStatus())
        );
    }
}
