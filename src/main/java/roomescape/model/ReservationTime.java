package roomescape.model;

import roomescape.service.dto.ReservationTimeDto;

import java.time.LocalTime;
import java.util.Objects;

public class ReservationTime {

    private long id;
    private LocalTime startAt;

    public ReservationTime(long id, LocalTime startAt) {
        validate(id, startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private ReservationTime(LocalTime startAt) {
        validateNull(startAt);
        this.id = 0;
        this.startAt = startAt;
    }

    private ReservationTime() {
    }

    public static ReservationTime from(ReservationTimeDto reservationTimeDto) {
        return new ReservationTime(reservationTimeDto.getStartAt());
    }

    private void validate(long id, LocalTime startAt) {
        validateRange(id);
        validateNull(startAt);
    }

    private void validateRange(long id) {
        if (id <= 0) {
            throw new IllegalStateException("id는 0 이하일 수 없습니다.");
        }
    }

    private void validateNull(LocalTime value) {
        if (value == null) {
            throw new IllegalStateException("데이터는 null일 수 없습니다.");
        }
    }

    public long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationTime that = (ReservationTime) o;
        return id == that.id && Objects.equals(startAt, that.startAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startAt);
    }

    @Override
    public String toString() {
        return "ReservationTime{" +
                "id=" + id +
                ", startAt=" + startAt +
                '}';
    }
}
