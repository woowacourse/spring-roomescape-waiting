package roomescape.reservationtime.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"start_at", "finish_at"}))
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "start_at", nullable = false)
    private LocalTime startAt;

    @Column(name = "finish_at", nullable = false)
    private LocalTime finishAt;

    protected ReservationTime() {
    }

    private ReservationTime(Long id, LocalTime startAt, LocalTime finishAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("시작 시간은 필수입니다.");
        }
        if (finishAt == null) {
            throw new IllegalArgumentException("종료 시간은 필수입니다.");
        }
        this.id = id;
        this.startAt = startAt;
        this.finishAt = finishAt;
    }

    public static ReservationTime of(LocalTime startAt, LocalTime finishAt) {
        return new ReservationTime(null, startAt, finishAt);
    }

    public static ReservationTime restore(Long id, LocalTime startAt, LocalTime finishAt) {
        return new ReservationTime(id, startAt, finishAt);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public LocalTime getFinishAt() {
        return finishAt;
    }
}