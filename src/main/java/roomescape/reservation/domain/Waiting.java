package roomescape.reservation.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Waiting waiting)) {
            return false;
        }
        return id != null && id.equals(waiting.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
