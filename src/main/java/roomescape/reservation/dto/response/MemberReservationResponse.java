package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.reservation.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record MemberReservationResponse(
        Long reservationId, String theme, LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time, String status) {
    public static MemberReservationResponse from(final Reservation reservation) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt(),
                convertReservationStatus(reservation)
        );
    }

    private static String convertReservationStatus(final Reservation reservation) {
        if (reservation.isReserved()) {
            return "예약";
        }
        return "예약대기";
    }
}
