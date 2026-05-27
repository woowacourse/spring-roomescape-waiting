package roomescape.reservation.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import roomescape.global.exception.RoomEscapeException;

@Getter
@EqualsAndHashCode(of = {"name", "slot"})
public class Waiting {

    private final Long id;
    private final String name;
    private final ReservationSlot slot;

    @Builder
    public Waiting(Long id, String name, ReservationSlot slot) {
        this.id = id;
        this.name = requireName(name);
        this.slot = slot;
    }

    public Waiting withId(Long generatedId) {
        return Waiting.builder()
                .id(generatedId)
                .name(this.name)
                .slot(this.slot)
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
