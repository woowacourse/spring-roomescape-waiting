package roomescape.reservation.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import roomescape.common.domain.ReservationSlot;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorCode;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class Reservation {

    private final Long id;
    private final String name;
    private final ReservationSlot slot;

    @Builder(access = lombok.AccessLevel.PRIVATE)
    private Reservation(Long id, String name, ReservationSlot slot) {
        this.id = id;
        this.name = name;
        this.slot = slot;
    }

    public static Reservation restore(Long id, String name, ReservationSlot slot) {
        return Reservation.builder()
                .id(id)
                .name(name)
                .slot(slot)
                .build();
    }

    public Reservation reschedule(LocalDate date, ReservationTime time, Clock clock) {
        Reservation changed = Reservation.builder()
                .id(this.id)
                .name(this.name)
                .slot(new ReservationSlot(date, time, getTheme()))
                .build();
        if (changed.isPast(clock)) {
            throw new BusinessException(ErrorCode.PAST_TIME_RESERVATION);
        }
        return changed;
    }

    public boolean isPast(Clock clock) {
        return LocalDateTime.of(getDate(), getTime().getStartAt()).isBefore(LocalDateTime.now(clock));
    }

    public void validateModifiable(Clock clock, ErrorCode code) {
        if (!LocalDateTime.now(clock).isBefore(LocalDateTime.of(getDate(), getTime().getStartAt()).minusHours(12))) {
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
