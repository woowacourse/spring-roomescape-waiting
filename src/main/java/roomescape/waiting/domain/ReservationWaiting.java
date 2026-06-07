package roomescape.waiting.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import roomescape.global.exception.ForbiddenException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.exception.ReservationWaitingErrorCode;

public class ReservationWaiting implements Comparable<ReservationWaiting> {
    private final Long id;
    private final String name;
    private final ReservationSlot slot;
    private final LocalDateTime requestedAt;

    public ReservationWaiting(Long id, String name, ReservationSlot slot, LocalDateTime requestedAt) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.requestedAt = requestedAt;
        validateExpiry(requestedAt);
    }

    public ReservationWaiting(String name, LocalDate date, ReservationTime time, Theme theme,
                              LocalDateTime requestTime) {
        this(null, name, new ReservationSlot(date, time, theme), requestTime);
    }

    public void validateDeletable(String name, LocalDateTime current) {
        validateOwner(name);
        validateExpiry(current);
    }

    private void validateExpiry(LocalDateTime current) {
        this.slot.validateNotExpired(current);
    }

    public Reservation toReservation(LocalDateTime requestTime) {
        return new Reservation(name, slot.date(), slot.time(), slot.theme(), requestTime);
    }

    private void validateOwner(String userName) {
        if (!Objects.equals(this.name, userName)) {
            throw new ForbiddenException(ReservationWaitingErrorCode.AUTHORIZATION_FAIL);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ReservationSlot getSlot() {
        return slot;
    }

    public LocalDate getDate() {
        return slot.date();
    }

    public ReservationTime getTime() {
        return slot.time();
    }

    public Theme getTheme() {
        return slot.theme();
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationWaiting that = (ReservationWaiting) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(ReservationWaiting other) {
        return Comparator.comparing(ReservationWaiting::getRequestedAt)
                .thenComparing(ReservationWaiting::getId, Comparator.nullsLast(Long::compareTo))
                .compare(this, other);
    }
}
