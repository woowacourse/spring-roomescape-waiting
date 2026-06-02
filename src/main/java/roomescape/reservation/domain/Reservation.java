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

    public Reservation(Long id, String name, ReservationSlot slot) {
        this.id = id;
        this.name = name;
        this.slot = slot;
    }

    public static Reservation of(String name, LocalDate date, ReservationTime time, Theme theme) {
        Reservation reservation = new Reservation(null, name, new ReservationSlot(date, time, theme));
        reservation.validateExpiry();
        return reservation;
    }

    public Reservation update(LocalDate newDate, ReservationTime newTime, String userName) {
        validateOwner(userName);
        validateExpiry();

        LocalDate targetDate = getNewDateValue(newDate);
        ReservationTime targetTime = getNewReservationTimeValue(newTime);

        Reservation updated = new Reservation(
                this.id,
                this.name,
                new ReservationSlot(targetDate, targetTime, this.slot.theme())
        );
        updated.validateExpiry();

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

    public void validateOwner(String name) {
        if (!this.name.equals(name)) {
            throw new ForbiddenException(ReservationErrorCode.AUTHORIZATION_FAIL.getMessage());
        }
    }

    public void validateExpiry() {
        LocalDate today = LocalDate.now();
        if (this.slot.date().isBefore(today)) {
            throw new InvalidBusinessStateException(ReservationErrorCode.INVALID_DATE.getMessage());
        }
        LocalDateTime current = LocalDateTime.now();

        LocalDateTime targetTime = LocalDateTime.of(getDate(), getTime().getStartAt());
        if (current.isAfter(targetTime)) {
            throw new InvalidBusinessStateException(ReservationErrorCode.INVALID_TIME.getMessage());
        }
    }

    public boolean hasSameName(String name) {
        return this.name.equals(name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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
