package roomescape.reservation.domain;

import java.time.LocalDate;
import roomescape.reservation.domain.exception.ReservationDateNullException;

public class ReservationDate {

    private final LocalDate reservationDate;

    public ReservationDate(LocalDate reservationDate) {
        validateNull(reservationDate);
        this.reservationDate = reservationDate;
    }

    private void validateNull(LocalDate reservationDate) {
        if (reservationDate == null) {
            throw new ReservationDateNullException("[ERROR] 날짜는 비어있을 수 없습니다.");
        }
    }

    public LocalDate getDate() {
        return reservationDate;
    }
}
