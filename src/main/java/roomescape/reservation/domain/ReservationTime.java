package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import roomescape.exceptions.MissingRequiredFieldException;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(Long id, LocalTime startAt) {
        validate(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    private void validate(LocalTime startAt) {
        if (startAt == null) {
            throw new MissingRequiredFieldException("시작 시간은 필수 값입니다.");
        }
    }

    public boolean isBeforeNow(LocalDate date) {
        LocalDateTime dateTimeToReserve = LocalDateTime.of(date, startAt);
        LocalDateTime now = LocalDateTime.now();

        return dateTimeToReserve.isBefore(now);
    }

    public boolean isBelongTo(List<Long> timeIds) {
        return timeIds.contains(id);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public String getStartAt(DateTimeFormatter formatter) {
        return startAt.format(formatter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTime that = (ReservationTime) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
