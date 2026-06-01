package roomescape.reservationtime.domain;

import lombok.Getter;
import roomescape.common.exception.DomainException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import static roomescape.common.domain.DomainPreconditions.require;
import static roomescape.common.domain.DomainPreconditions.requireNonNull;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;

@Getter
public class ReservationTime {
    private final Long id;
    private final LocalTime startAt;
    private final LocalDateTime deletedAt;

    private ReservationTime(Long id, LocalTime startAt, LocalDateTime deletedAt) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
        this.deletedAt = deletedAt;
    }

    public static ReservationTime create(LocalTime startAt) {
        return new ReservationTime(null, startAt, null);
    }

    public static ReservationTime of(long id, LocalTime startAt) {
        return of(id, startAt, null);
    }

    public static ReservationTime of(long id, LocalTime startAt, LocalDateTime deletedAt) {
        return new ReservationTime(id, startAt, deletedAt);
    }

    public ReservationTime withId(long id) {
        require(this.id == null, new DomainException(RESERVATION_TIME_ALREADY_HAS_ID));
        return of(id, startAt, deletedAt);
    }

    private void validateStartAt(LocalTime startAt) {
        requireNonNull(startAt, new DomainException(INVALID_RESERVATION_TIME));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationTime that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
