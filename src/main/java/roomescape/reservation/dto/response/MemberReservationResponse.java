package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationDetail;

import java.time.LocalDate;
import java.time.LocalTime;

public record MemberReservationResponse(
        Long reservationId, String theme, LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time, String status) {
    public static MemberReservationResponse fromEntity(final MemberReservation memberReservation, final long order) {
        ReservationDetail reservation = memberReservation.getReservationDetail();
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt(),
                convertReservationWaitingOrderStatus(order)
        );
    }

    private static String convertReservationWaitingOrderStatus(final long order) {
        if (order == 0) {
            return "예약";
        }
        return String.format("%d번째 예약대기", order);
    }
}
