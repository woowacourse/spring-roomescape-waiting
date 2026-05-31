package roomescape.domain;

import java.time.LocalTime;
import java.util.Objects;

import roomescape.domain.exception.DomainPreconditions;
import roomescape.domain.exception.DomainErrorCode;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(Long id, LocalTime startAt) {
        validate(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public static ReservationTime of(Long id, ReservationTime reservationTime) {
        return new ReservationTime(id, reservationTime.startAt);
    }

    private void validate(LocalTime startAt) {
        DomainPreconditions.requireNonNull(startAt, DomainErrorCode.INVALID_INPUT, "startAt");
    }

    public boolean isPast(LocalTime localTime) {
        return startAt.isBefore(localTime);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ReservationTime that = (ReservationTime) object;
        return Objects.equals(id, that.id) && Objects.equals(startAt, that.startAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startAt);
    }
}
