package roomescape.reservation.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import roomescape.global.exception.RoomEscapeException;

@Getter
@EqualsAndHashCode(of = {"memberName", "slot"})
public class Waiting {

    private final Long id;
    private final MemberName memberName;
    private final ReservationSlot slot;

    @Builder
    public Waiting(Long id, MemberName memberName, ReservationSlot slot) {
        this.id = id;
        this.memberName = memberName;
        this.slot = slot;
    }

    public Waiting withId(Long generatedId) {
        return Waiting.builder()
                .id(generatedId)
                .memberName(this.memberName)
                .slot(this.slot)
                .build();
    }
}
