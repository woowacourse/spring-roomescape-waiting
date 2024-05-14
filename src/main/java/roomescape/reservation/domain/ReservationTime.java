package roomescape.reservation.domain;

import jakarta.persistence.*;
import roomescape.global.exception.ViolationException;

import java.time.LocalTime;

@Entity
public class ReservationTime {
    private static final int TIME_UNIT = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalTime startAt;

    protected ReservationTime() {
    }

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime(Long id, ReservationTime time) {
        this.id = id;
        this.startAt = time.startAt;
    }

    public ReservationTime(Long id, LocalTime startAt) {
        validateTimeUnit(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private void validateTimeUnit(LocalTime time) {
        if (time.getMinute() % TIME_UNIT != 0) {
            throw new ViolationException("예약 시간은 10분 단위입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
