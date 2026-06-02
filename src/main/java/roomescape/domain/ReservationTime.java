package roomescape.domain;

import roomescape.util.Validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(Long id, LocalTime startAt) {
        validateStartAt(startAt);

        this.id = id;
        this.startAt = startAt;
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean isPast(LocalDate date) {
        return LocalDateTime.of(date, startAt).isBefore(LocalDateTime.now());
    }

    private void validateStartAt(LocalTime startAt) {
        Validator.notNull(startAt, "예약 시작 시간은 비어 있을 수 없습니다.");
    }
}
