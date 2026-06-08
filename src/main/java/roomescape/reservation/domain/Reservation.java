package roomescape.reservation.domain;

import lombok.Getter;
import roomescape.global.exception.InvalidRequestException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Reservation {
    private final Long id;
    private final String name;
    private final Slot slot;
    private final Long requestOrder;

    public static Reservation create(String name,
                                     LocalDate date,
                                     ReservationTime time,
                                     Theme theme,
                                     LocalDateTime now) {
        return create(name, new Slot(date, time, theme), now);
    }

    private static Reservation create(String name, Slot slot, LocalDateTime now) {
        validateNewReservation(now, name, slot);
        return new Reservation(null, name, slot, null);
    }

    public static Reservation reconstruct(Long id, String name, Slot slot) {
        return reconstruct(id, name, slot, null);
    }

    public static Reservation reconstruct(Long id, String name, Slot slot, Long requestOrder) {
        validateSavedReservation(name, slot);
        return new Reservation(id, name, slot, requestOrder);
    }

    private Reservation(Long id, String name, Slot slot, Long requestOrder) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.requestOrder = requestOrder;
    }

    public Reservation updateDateTime(LocalDate date, ReservationTime time, LocalDateTime now) {
        Slot updatedSlot = new Slot(date, time, slot.theme());
        validatePastDateTime(now, updatedSlot);

        return reconstruct(id, name, updatedSlot, requestOrder);
    }

    public Reservation withId(Long id) {
        validateId(id);

        if (this.id != null) {
            throw new InvalidRequestException("이미 식별자가 존재하는 예약입니다.");
        }

        return reconstruct(id, name, slot, requestOrder);
    }

    private static void validateNewReservation(
            LocalDateTime now,
            String name,
            Slot slot) {
        validateName(name);
        validateSlot(slot);
        validatePastDateTime(now, slot);
    }

    private static void validateSavedReservation(String name, Slot slot) {
        validateName(name);
        validateSlot(slot);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("예약자 이름은 비어 있을 수 없습니다.");
        }
    }

    private static void validateSlot(Slot slot) {
        if (slot == null) {
            throw new InvalidRequestException("예약 슬롯은 비어 있을 수 없습니다.");
        }
    }

    private static void validatePastDateTime(LocalDateTime now, Slot slot) {
        if (slot.isPast(now)) {
            throw new InvalidRequestException("현재 시각 이후의 날짜와 시간을 선택해주세요.");
        }
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new InvalidRequestException("예약 id는 비어 있을 수 없습니다.");
        }
    }

    public boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }

    public boolean hasSameDateTime(LocalDate date, ReservationTime time) {
        return slot.hasSameDateTime(date, time);
    }

    public boolean isReservedBy(String name) {
        return Objects.equals(this.name, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
