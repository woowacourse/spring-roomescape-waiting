package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Table(name= "reservation_time")
@Entity
public class ReservationTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalTime startAt;

    public ReservationTime(
            Long id, LocalTime startAt) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime() {
    }

    public static ReservationTime createWithoutId(LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }

    void validateStartAt(LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("[ERROR] 시작 시간을 반드시 입력해야 합니다. 예시) HH:MM");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
