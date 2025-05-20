package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record MyReservationResponse(Long reservationId, String theme, LocalDate date, LocalTime time, String status) {

    private final static String RESERVATION_STATUS = "예약";

    public static MyReservationResponse from(final Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                RESERVATION_STATUS
        );
    }
}
