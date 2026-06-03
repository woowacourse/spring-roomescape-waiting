package roomescape.reservationtime.domain;

import static roomescape.common.domain.DomainPreconditions.require;
import static roomescape.common.domain.DomainPreconditions.requireNonNull;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.INVALID_RESERVATION_TIME;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.RESERVATION_TIME_ALREADY_HAS_ID;

import java.time.LocalTime;
import java.util.Objects;
import lombok.Getter;
import roomescape.common.exception.DomainException;

@Getter
public class ReservationTime {
    private final Long id;
    private final LocalTime startAt;

    private ReservationTime(Long id, LocalTime startAt) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public static ReservationTime create(LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }

    public static ReservationTime of(long id, LocalTime startAt) {
        return new ReservationTime(id, startAt);
    }

    public ReservationTime withId(long id) {
        require(this.id == null, new DomainException(RESERVATION_TIME_ALREADY_HAS_ID));
        return of(id, startAt);
    }

    private void validateStartAt(LocalTime startAt) {
        requireNonNull(startAt, new DomainException(INVALID_RESERVATION_TIME));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReservationTime that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
