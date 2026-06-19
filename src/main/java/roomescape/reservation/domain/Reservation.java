package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.global.exception.ForbiddenException;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public class Reservation {
    private final Long id;
    private final String name;
    private final ReservationSlot slot;
    private final LocalDateTime updatedAt;
    private final boolean confirmed;

    public Reservation(Long id, String name, ReservationSlot slot, LocalDateTime updatedAt, boolean confirmed) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.updatedAt = updatedAt;
        this.confirmed = confirmed;
        validateExpiry(updatedAt);
    }

    public Reservation(String name, ReservationSlot reservationSlot, LocalDateTime requestTime) {
        this(null, name, reservationSlot, requestTime, false);
    }

    public Reservation confirm() {
        return new Reservation(id, name, slot, updatedAt, true);
    }

    public Reservation update(LocalDate newDate, ReservationTime newTime, String userName, LocalDateTime requestTime) {
        validateOwner(userName);
        validateExpiry(requestTime);

        ReservationSlot targetSlot = generateTemporalSlot(newDate, newTime);

        Reservation updated = new Reservation(
                this.id,
                this.name,
                targetSlot,
                requestTime,
                this.confirmed
        );
        updated.validateExpiry(requestTime);
        return updated;
    }

    public ReservationSlot generateTemporalSlot(LocalDate newDate, ReservationTime newTime) {
        return new ReservationSlot(newDate, newTime, this.slot.theme());
    }

    public void validateDeletableByUser(String name, LocalDateTime requestTime) {
        validateOwner(name);
        validateExpiry(requestTime);
    }

    public void validateDeletableByAdmin(LocalDateTime requestTime) {
        validateExpiry(requestTime);
    }

    private void validateOwner(String name) {
        if (!this.name.equals(name)) {
            throw new ForbiddenException(ReservationErrorCode.AUTHORIZATION_FAIL);
        }
    }

    private void validateExpiry(LocalDateTime requestTime) {
        this.slot.validateNotExpired(requestTime);
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

    public boolean isConfirmed() {
        return confirmed;
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
