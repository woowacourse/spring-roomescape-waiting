package roomescape.waiting.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import roomescape.global.exception.ForbiddenException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.exception.ReservationWaitingErrorCode;

public class ReservationWaiting implements Comparable<ReservationWaiting> {
    private final Long id;
    private final String name;
    private final ReservationSlot slot;
    private final LocalDateTime updatedAt;

    public ReservationWaiting(Long id, String name, ReservationSlot slot, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.updatedAt = updatedAt;

        validateExpiry(this.updatedAt);
    }

    public ReservationWaiting(String name, LocalDate date, ReservationTime time, Theme theme,
                              LocalDateTime requestTime) {
        this(null, name, new ReservationSlot(date, time, theme), requestTime);
    }

    public void validateExpiry(LocalDateTime current) {
        if (this.slot.isDateBefore(current.toLocalDate())) {
            throw new InvalidBusinessStateException(ReservationWaitingErrorCode.INVALID_DATE);
        }

        if (this.slot.isExpired(current)) {
            throw new InvalidBusinessStateException(ReservationWaitingErrorCode.INVALID_TIME);
        }
    }

    public void validateOwner(String userName) {
        if (!this.name.equals(userName)) {
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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
        return Comparator.comparing(ReservationWaiting::getUpdatedAt)
                .thenComparing(ReservationWaiting::getId)
                .compare(this, other);
    }
}
