package roomescape.domain;

import java.time.LocalDateTime;
import roomescape.domain.exception.PastReservationException;

public record ReservationDateTime(ReservationDate reservationDate, ReservationTime reservationTime) {

    public static ReservationDateTime create(ReservationDate reservationDate, ReservationTime reservationTime, LocalDateTime now) {
        validatePast(reservationDate, reservationTime, now);
        return new ReservationDateTime(reservationDate, reservationTime);
    }

    private static void validatePast(ReservationDate reservationDate, ReservationTime reservationTime, LocalDateTime now) {
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate.getDate(), reservationTime.getStartAt());

        if (reservationDateTime.isBefore(now)) {
            throw new PastReservationException("[ERROR] 현재 시간 이후로 예약할 수 있습니다.");
        }
    }
}
