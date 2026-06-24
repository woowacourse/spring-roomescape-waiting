package roomescape.reservationwaiting.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.common.domain.ReservationSlot;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorCode;
import roomescape.reservation.domain.PaymentStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class ReservationWaiting {
    private final Long id;
    private final String name;
    private final ReservationSlot slot;

    private ReservationWaiting(Long id, String name, ReservationSlot slot) {
        this.id = id;
        this.name = name;
        this.slot = slot;
    }

    public static ReservationWaiting restore(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        return new ReservationWaiting(id, name, new ReservationSlot(date, time, theme));
    }

    public Reservation toReservation() {
        return Reservation.restore(null, name, slot, PaymentStatus.CONFIRMED);
    }

    public void validateCancelable(Clock clock, ErrorCode code) {
        if (LocalDateTime.of(getDate(), getTime().getStartAt()).isBefore(LocalDateTime.now(clock))) {
            throw new BusinessException(code);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return this.slot.date();
    }

    public ReservationTime getTime() {
        return this.slot.time();
    }

    public Theme getTheme() {
        return this.slot.theme();
    }

    public ReservationSlot getSlot() {
        return this.slot;
    }
}
