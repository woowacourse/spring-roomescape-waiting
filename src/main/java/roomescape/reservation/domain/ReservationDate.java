package roomescape.reservation.domain;

import java.time.LocalDate;

public record ReservationDate(LocalDate date) {
    public ReservationDate {
        if (date == null) {
            throw new NullPointerException("date는 null 일 수 없습니다.");
        }
    }

    public boolean isBefore(final LocalDate date) {
        return this.date.isBefore(date);
    }
}
