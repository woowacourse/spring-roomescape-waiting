package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record MemberReservation(
        Long id,
        String themeName,
        LocalDate date,
        LocalTime time,
        String status) {

    public MemberReservation(Reservation reservation, int waitingNumber) {
        this(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                statusMessage(waitingNumber)
        );
    }

    private static String statusMessage(int waitingNumber) {
        if (waitingNumber > 1) {
            return waitingNumber + "번째 예약 대기";
        }
        return "예약";
    }
}
