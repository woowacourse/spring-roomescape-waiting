package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import roomescape.global.exception.RoomEscapeException;

@Getter
public class Reservation {

    private final Long id;
    private final String name;
    private final ReservationSlot slot;

    @Builder
    public Reservation(Long id, String name, ReservationSlot slot) {
        this.id = id;
        this.name = requireName(name);
        this.slot = slot;
    }

    public Reservation withId(Long generatedId) {
        return Reservation.builder()
                .id(generatedId)
                .name(this.name)
                .slot(this.slot)
                .build();
    }

    public Reservation updateDateAndTime(LocalDate date, Long timeId, LocalTime startAt, LocalDateTime now) {
        ReservationSlot updatedSlot = slot.updateDateAndTime(date, timeId, startAt);
        updatedSlot.validateReservable(now);

        return Reservation.builder()
                .id(this.id)
                .name(this.name)
                .slot(updatedSlot)
                .build();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Reservation reservation)) {
            return false;
        }
        return id != null && id.equals(reservation.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new RoomEscapeException("이름은 비어있을 수 없습니다.");
        }
        return name;
    }
}
