package roomescape.reservationtime.domain;

import roomescape.common.exception.DomainException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import static roomescape.common.domain.DomainPreconditions.require;
import static roomescape.common.domain.DomainPreconditions.requireNonNull;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;

public class ReservationTime {
    private final Long id;
    private final LocalTime startAt;
    private final LocalDateTime deletedAt;

    public ReservationTime(Long id, LocalTime startAt) {
        this(id, startAt, null);
    }

    public ReservationTime(Long id, LocalTime startAt, LocalDateTime deletedAt) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
        this.deletedAt = deletedAt;
    }

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime withId(Long id) {
        requireNonNull(id, new DomainException(INVALID_RESERVATION_TIME_ID));
        require(this.id == null, new DomainException(RESERVATION_TIME_ALREADY_HAS_ID));

        return new ReservationTime(id, startAt, deletedAt);
    }

    private void validateStartAt(LocalTime startAt) {
        requireNonNull(startAt, new DomainException(INVALID_RESERVATION_TIME));
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
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
