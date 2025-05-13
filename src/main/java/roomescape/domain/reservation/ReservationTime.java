package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import roomescape.domain.BusinessRuleViolationException;

@Entity
public record ReservationTime(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id,
        LocalTime startAt
) {

    private static final LocalTime RESERVATION_START_TIME = LocalTime.of(12, 0);
    private static final LocalTime RESERVATION_END_TIME = LocalTime.of(22, 0);

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime {
        if (startAt.isBefore(RESERVATION_START_TIME) || startAt.isAfter(RESERVATION_END_TIME)) {
            throw new BusinessRuleViolationException("해당 시간은 예약 가능 시간이 아닙니다.");
        }
    }
}
