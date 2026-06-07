package roomescape.domain;

import java.time.LocalDate;

public record ReservationDate(
        LocalDate date
) {

    public ReservationDate {
        validate(date);
    }

    private void validate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("예약 날짜는 비워둘 수 없습니다.");
        }
    }

    public boolean isPast() {
        return date.isBefore(LocalDate.now());
    }

    public boolean isToday() {
        return date.isEqual(LocalDate.now());
    }
}
