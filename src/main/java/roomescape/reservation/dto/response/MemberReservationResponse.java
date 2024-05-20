package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record MemberReservationResponse(
        Long reservationId, String theme, LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time, String status) {
    public static MemberReservationResponse from(final MemberReservation memberReservation) {
        Reservation reservation = memberReservation.getReservation();
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt(),
                convertReservationStatus(memberReservation)
        );
    }

    private static String convertReservationStatus(final MemberReservation memberReservation) {
        if (memberReservation.isReserved()) {
            return "예약";
        }
        return "예약대기";
    }
}
