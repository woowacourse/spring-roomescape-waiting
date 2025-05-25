package roomescape.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class ReservationTime {

    private static final LocalTime RUNNING_TIME = LocalTime.of(2, 0);
    private static final LocalTime OPERATING_START = LocalTime.of(10, 0);
    private static final LocalTime OPERATING_END = LocalTime.of(22, 0);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalTime startAt;

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public boolean isDuplicatedWith(ReservationTime other) {
        LocalTime otherStartAt = other.startAt;
        int interval = Math.abs(otherStartAt.toSecondOfDay() - startAt.toSecondOfDay());
        return interval < RUNNING_TIME.toSecondOfDay();
    }

    public boolean isAvailable() {
        return !(startAt.isBefore(OPERATING_START) || startAt.isAfter(OPERATING_END));
    }
}
