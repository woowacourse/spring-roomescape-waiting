package roomescape.reservationTime.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.common.exception.BusinessException;

@Entity
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalTime startAt;

    protected ReservationTime() {
    }

    private ReservationTime(final Long id, final LocalTime startAt) {
        validateIsNonNull(startAt);

        this.id = id;
        this.startAt = startAt;
    }

    private void validateIsNonNull(final Object object) {
        if (object == null) {
            throw new BusinessException("시간 정보는 null 일 수 없습니다.");
        }
    }

    public static ReservationTime createWithoutId(final LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }

    public static ReservationTime createWithId(final Long id, final LocalTime startAt) {
        validateIdIsNonNull(id);
        return new ReservationTime(Objects.requireNonNull(id), startAt);
    }

    private static void validateIdIsNonNull(final Long id) {
        if (id == null) {
            throw new BusinessException("시간 id는 null 일 수 없습니다.");
        }
    }

    public boolean isBefore(final LocalTime time) {
        return this.startAt.isBefore(time);
    }

    public boolean isEqual(final LocalTime time) {
        return startAt.equals(time);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof ReservationTime that)) {
            return false;
        }

        if (getId() == null && that.getId() == null) {
            return false;
        }
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
