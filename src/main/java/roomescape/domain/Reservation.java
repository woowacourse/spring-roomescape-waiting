package roomescape.domain;

import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ReservationErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Reservation {
    private static final int CANCEL_DEADLINE_HOURS = 24;

    private final Long id;
    private final String name;
    private final ReservationSlot slot;

    public Reservation(Long id, String name, ReservationSlot slot) {
        Objects.requireNonNull(name, "예약자명은 필수값 입니다.");
        Objects.requireNonNull(slot, "예약 슬롯은 필수값 입니다.");

        this.id = id;
        this.name = name;
        this.slot = slot;
    }

    public static Reservation createWithoutId(String name, ReservationSlot slot) {
        return new Reservation(null, name, slot);
    }

    public void validateCancelable(LocalDateTime now) {
        Objects.requireNonNull(now, "현재 시간은 필수값 입니다.");

        LocalDateTime reservationDateTime = LocalDateTime.of(slot.getDate(), slot.getTime().getStartAt());
        LocalDateTime cancelDeadLine = reservationDateTime.minusHours(CANCEL_DEADLINE_HOURS);
        if (now.isAfter(cancelDeadLine)) {
            throw new RoomEscapeException(ReservationErrorCode.CANNOT_CANCEL);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public ReservationSlot getSlot(){
        return slot;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Reservation reservation = (Reservation) object;
        if (id != null && reservation.id != null) {
            return Objects.equals(id, reservation.id);
        }
        return Objects.equals(name, reservation.name)
                && Objects.equals(slot, reservation.slot);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(name, slot);
    }
}
