package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import roomescape.global.exception.RoomescapeException;

@Embeddable
public class Time {

    @Column(name = "start_at")
    private LocalTime time;

    public Time(String rawTime) {
        try {
            this.time = LocalTime.parse(rawTime);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new RoomescapeException("잘못된 시간 형식입니다.");
        }
    }

    protected Time() {
    }

    public boolean isBefore(LocalTime localTime) {
        return time.isBefore(localTime);
    }

    public LocalTime getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Time time1 = (Time) o;
        return Objects.equals(time, time1.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time);
    }
}
