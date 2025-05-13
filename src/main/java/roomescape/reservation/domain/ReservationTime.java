package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservation_times")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = {"id"})
public class ReservationTime {

    private static final LocalTime OPEN_TIME = LocalTime.of(10, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(22, 0);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalTime startAt;

    public ReservationTime(final Long id, final LocalTime startAt) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(final LocalTime startAt) {
        this(null, startAt);
    }

    private void validateStartAt(final LocalTime startAt) {
        validateNull(startAt);
        validateTimeRange(startAt);
    }

    private void validateNull(LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("예약 시간은 null일 수 없습니다.");
        }
    }

    private void validateTimeRange(final LocalTime startAt) {
        if (startAt.isBefore(OPEN_TIME) || startAt.isAfter(CLOSE_TIME)) {
            throw new IllegalArgumentException(
                    String.format("예약 시간은 %s ~ %s 사이여야 합니다. startAt: %s", OPEN_TIME, CLOSE_TIME, startAt));
        }
    }
}
