package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationWithWaiting;

public record MemberReservationResponse(
        Long id,
        String themeName,
        LocalDate date,
        LocalTime time,
        String status) {

    public MemberReservationResponse(ReservationWithWaiting reservation) {
        this(
                reservation.getReservationId(),
                reservation.getThemeName(),
                reservation.getReservationDate(),
                reservation.getStartAt(),
                statusMessage(reservation.getWaitingNumber())
        );
    }

    private static String statusMessage(int waitingNumber) {
        if (waitingNumber > 1) {
            return waitingNumber + "번째 예약 대기";
        }
        return "예약";
    }
}
