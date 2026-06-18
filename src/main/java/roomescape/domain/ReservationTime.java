package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import roomescape.domain.exception.InvalidDomainException;

@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 컬럼명은 네이밍 전략에 위임: startAt -> start_at. UNIQUE(start_at) 유지.
    @Column(nullable = false, unique = true)
    private LocalTime startAt;

    protected ReservationTime() {
    }

    private ReservationTime(Long id, LocalTime startAt) {
        validate(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public static ReservationTime create(LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }

    public static ReservationTime withId(Long id, LocalTime startAt) {
        return new ReservationTime(id, startAt);
    }

    private static void validate(LocalTime startAt) {
        validateStartAt(startAt);
    }

    private static void validateStartAt(LocalTime startAt) {
        if (startAt == null) {
            throw new InvalidDomainException("예약 시간은 비어 있을 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
