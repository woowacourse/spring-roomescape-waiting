package roomescape.time.domain;

import java.time.LocalTime;
import java.util.Objects;
import roomescape.exception.BadRequestException;
import roomescape.exception.IllegalReservationDateTimeRequestException;

public class Time {

    private static final LocalTime OPEN_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(23, 0);
    private final LocalTime startAt;
    private long id;

    public Time(LocalTime startAt) {
        this(0, startAt);
        validation(startAt);
    }

    public Time(long id, LocalTime startAt) {
        validation(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public void validation(LocalTime startAt) {
        if (startAt == null) {
            throw new BadRequestException("시간 값이 정의되지 않은 요청입니다.");
        }
        if (OPEN_TIME.isAfter(startAt) || CLOSE_TIME.isBefore(startAt)) {
            throw new IllegalReservationDateTimeRequestException("운영 시간 외의 예약 시간 요청입니다.");
        }
    }

    public Time(long id) {
        this(id, null);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Time time = (Time) o;
        return id == time.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
