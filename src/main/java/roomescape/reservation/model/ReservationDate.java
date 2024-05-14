package roomescape.reservation.model;

import java.time.LocalDate;

public record ReservationDate(LocalDate value) {

    public ReservationDate {
        validateDate(value);
    }

    private void validateDate(final LocalDate value) {
        if (value == null) {
            throw new IllegalArgumentException("예약 날짜는 공백을 입력할 수 없습니다.");
        }
    }
}
