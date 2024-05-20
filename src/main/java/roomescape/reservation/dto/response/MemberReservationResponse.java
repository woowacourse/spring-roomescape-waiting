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
                "예약"
        );
    }

    public static MemberReservationResponse ofMemberReservationAndOrder(final MemberReservation memberReservation, final long order) {
        Reservation reservation = memberReservation.getReservation();
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt(),
                convertReservationWaitingOrderStatus(order)
        );
    }

    private static String convertReservationWaitingOrderStatus(final long order) {
        return String.format("%d번째 예약대기", order);
    }
}
