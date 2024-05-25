package roomescape.time.domain;

import java.time.LocalTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import roomescape.exception.BadRequestException;

@Entity
public class Time {
    private static final LocalTime OPEN_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(23, 0);
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(nullable = false, unique = true, name = "start_at")
    private LocalTime startAt;

    public Time() {
    }

    public Time(LocalTime startAt) {
        this(null, startAt);
        validation(startAt);
    }

    public Time(Long id, LocalTime startAt) {
        validation(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public void validation(LocalTime startAt) {
        if (startAt == null) {
            throw new BadRequestException("시간 값이 정의되지 않은 요청입니다.");
        }
        if (OPEN_TIME.isAfter(startAt) || CLOSE_TIME.isBefore(startAt)) {
            throw new BadRequestException("운영 시간 외의 예약 시간 요청입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Time time)) return false;

        if (id == null || time.id == null) {
            return Objects.equals(startAt, time.startAt);
        }
        return Objects.equals(id, time.id);
    }

    @Override
    public int hashCode() {
        if (id == null) return Objects.hash(startAt);
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Time{" +
               "id=" + id +
               ", startAt=" + startAt +
               '}';
    }
}
