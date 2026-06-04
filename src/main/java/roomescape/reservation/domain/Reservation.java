package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.global.exception.ForbiddenException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public class Reservation {
    private final Long id;
    private final String name;
    private final ReservationSlot slot;
    private final LocalDateTime updatedAt;

    public Reservation(Long id, String name, ReservationSlot slot, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.updatedAt = updatedAt;
    }

    public Reservation(String name, LocalDate date, ReservationTime time, Theme theme, LocalDateTime requestTime) {
        this(null, name, new ReservationSlot(date, time, theme), requestTime);
        validateExpiry(requestTime);
    }

    public Reservation update(LocalDate newDate, ReservationTime newTime, String userName, LocalDateTime requestTime) {
        validateOwner(userName);
        validateExpiry(requestTime);

        LocalDate targetDate = getNewDateValue(newDate);
        ReservationTime targetTime = getNewReservationTimeValue(newTime);

        Reservation updated = new Reservation(
                this.id,
                this.name,
                new ReservationSlot(targetDate, targetTime, this.slot.theme()),
                requestTime
        );
        updated.validateExpiry(requestTime);
        return updated;
    }

    private LocalDate getNewDateValue(LocalDate newDate) {
        if (newDate == null) {
            return this.slot.date();
        }
        return newDate;
    }

    private ReservationTime getNewReservationTimeValue(ReservationTime newTime) {
        if (newTime == null) {
            return this.slot.time();
        }
        return newTime;
    }

    public void validateDeletable(String name, LocalDateTime requestTime) {
        if (name != null) {
            validateOwner(name);
        }
        validateExpiry(requestTime);
    }

    private void validateOwner(String name) {
        if (!this.name.equals(name)) {
            throw new ForbiddenException(ReservationErrorCode.AUTHORIZATION_FAIL);
        }
    }

    private void validateExpiry(LocalDateTime requestTime) {
        if (this.slot.isDateBefore(requestTime.toLocalDate())) {
            throw new InvalidBusinessStateException(ReservationErrorCode.INVALID_DATE);
        }

        if (this.slot.isExpired(requestTime)) {
            throw new InvalidBusinessStateException(ReservationErrorCode.INVALID_TIME);
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

    public Long getTimeId() {
        return slot.time().getId();
    }

    public Long getThemeId() {
        return slot.theme().getId();
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
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
