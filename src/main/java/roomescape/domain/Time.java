package roomescape.domain;

import roomescape.common.exception.InvalidInputException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class Time {
    private final Long id;
    private final LocalTime startAt;

    public Time(Long id, LocalTime startAt) {
        validate(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public Time(LocalTime startAt) {
        this(null, startAt);
    }

    private void validate(LocalTime startAt) {
        if (startAt.isBefore(LocalTime.of(10, 0)) || startAt.isAfter(LocalTime.of(22, 0))) {
            throw new InvalidInputException("영업 시간은 10시부터 22시 사이입니다.");
        }
    }

    public boolean isReservationBefore(LocalDateTime dateTime, LocalDate reservationDate) {
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, startAt);

        return reservationDateTime.isBefore(dateTime);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(startAt);
        return result;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Time time)) {
            return false;
        }

        return Objects.equals(id, time.id) && Objects.equals(startAt, time.startAt);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
