package roomescape.domain;

import static roomescape.domain.exception.DomainErrorCode.PAST_RESERVATION;
import static roomescape.domain.exception.DomainErrorCode.UNAUTHORIZED_RESERVATION;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.exception.RoomEscapeException;

public class Reservation {

    private final Long id;
    private final String name;
    private final Slot slot;

    public Reservation(Long id, String name, Slot slot) {
        this.id = id;
        this.name = name;
        this.slot = slot;
    }

    // TODO: Slot 전환이 완료되면 제거한다.
    public Reservation(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme
    ) {
        this(id, name, new Slot(date, time, theme));
    }

    public Reservation(String name, Slot slot) {
        this(null, name, slot);
    }

    // TODO: Slot 전환이 완료되면 제거한다.
    public Reservation(
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme
    ) {
        this(null, name, date, time, theme);
    }

    public void verifyReservable(LocalDateTime now) {
        if (isPast(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "과거 시점에 예약할 수 없습니다.");
        }
    }

    public boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }

    public void verifyCancelableBy(String name, LocalDateTime now) {
        verifyReservedBy(name, "본인의 예약만 취소할 수 있습니다.");
        if (isPast(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "이미 지난 예약은 취소할 수 없습니다.");
        }
    }

    public Reservation changeBy(String name, LocalDateTime now, LocalDate newDate, ReservationTime newTime) {
        verifyReservedBy(name, "본인의 예약만 변경할 수 있습니다.");
        if (isPast(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "이미 지난 예약은 변경할 수 없습니다.");
        }

        Slot newSlot = new Slot(newDate, newTime, slot.getTheme());

        if (newSlot.isPast(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "과거 시점으로 변경할 수 없습니다.");
        }

        return new Reservation(id, this.name, newSlot);
    }

    private void verifyReservedBy(String other, String message) {
        if (!this.name.equals(other)) {
            throw new RoomEscapeException(UNAUTHORIZED_RESERVATION, message);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Slot getSlot() {
        return slot;
    }

    public LocalDate getDate() {
        return slot.getDate();
    }

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }
}
