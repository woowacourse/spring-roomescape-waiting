package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import roomescape.global.exception.RoomEscapeException;

@Getter
@EqualsAndHashCode(of = {"name", "slot"})
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

    public Reservation updateDateAndTime(LocalDate date, Long timeId, LocalTime startAt) {
        return Reservation.builder()
                .id(this.id)
                .name(this.name)
                .slot(slot.updateDateAndTime(date, timeId, startAt))
                .build();
    }

    public void validateReservable(LocalDateTime now) {
        slot.validateReservable(now);
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new RoomEscapeException("이름은 비어있을 수 없습니다.");
        }
        return name;
    }
}
