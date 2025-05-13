package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class ReservationDateTime {

    private LocalDate date;
    @ManyToOne
    private ReservationTime time;

    public ReservationDateTime(final LocalDate date, final ReservationTime time) {
        validateDate(date);
        validateTime(time);
        this.date = date;
        this.time = time;
    }

    public ReservationDateTime() {
    }

    private void validateDate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜를 입력해야 합니다.");
        }
    }

    private void validateTime(final ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("시간을 입력해야 합니다.");
        }
    }

    public boolean isBefore(final LocalDateTime other) {
        if (date.isBefore(other.toLocalDate())) {
            return true;
        }
        if (date.equals(other.toLocalDate())) {
            return time.isBefore(other.toLocalTime());
        }
        return false;
    }

    public boolean isSameTime(final ReservationTime other) {
        return time.equals(other);
    }

    public boolean isBetween(final LocalDate from, final LocalDate to) {
        return (date.isAfter(from) || date.isEqual(from)) &&
                (date.isBefore(to) || date.isEqual(to));
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public long getTimeId() {
        return time.getId();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ReservationDateTime that = (ReservationDateTime) object;
        return Objects.equals(date, that.date) && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time);
    }
}
