package roomescape.waiting.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.global.exception.ForbiddenException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.exception.ReservationWaitingErrorCode;

public class ReservationWaiting {
    private final Long id;
    private final String name;
    private final ReservationSlot slot;

    private ReservationWaiting(Long id, String name, ReservationSlot slot) {
        this.id = id;
        this.name = name;
        this.slot = slot;
    }

    public static ReservationWaiting construct(String name, LocalDate date, ReservationTime time, Theme theme, LocalDateTime requestTime) {
        ReservationWaiting waiting = new ReservationWaiting(null, name, new ReservationSlot(date, time, theme));
        waiting.validateExpiry(requestTime);
        return waiting;
    }

    public static ReservationWaiting reconstruct(Long id, String name, ReservationSlot slot) {
        return new ReservationWaiting(id, name, slot);
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
}
