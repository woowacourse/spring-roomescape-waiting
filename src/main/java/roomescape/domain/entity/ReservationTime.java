package roomescape.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import java.util.Objects;

@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;

    private ReservationTime(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = Objects.requireNonNull(startAt, "시간이 필요합니다.");
    }

    protected ReservationTime() {
    }

    public static ReservationTime withId(Long id, LocalTime startAt) {
        if (id == null) {
            throw new IllegalArgumentException("id를 입력해주세요.");
        }

        return new ReservationTime(id, startAt);
    }

    public static ReservationTime withoutId(LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
