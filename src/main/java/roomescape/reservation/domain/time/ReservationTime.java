package roomescape.reservation.domain.time;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import roomescape.reservation.domain.util.ValidationUtils;

@Entity
@Getter
@NoArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;

    public ReservationTime(final Long id, final LocalTime startAt) {
        validate(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private void validate(final LocalTime startAt) {
        ValidationUtils.validateNonNull(startAt, "예약 시간을 입력해야 합니다.");
    }

    public ReservationTime(final LocalTime startAt) {
        this(null, startAt);
    }

    public boolean isBefore(final LocalTime other) {
        return startAt.isBefore(other) || startAt.equals(other);
    }
}
