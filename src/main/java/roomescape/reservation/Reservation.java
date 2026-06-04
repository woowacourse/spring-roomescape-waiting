package roomescape.reservation;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.slot.Slot;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Reservation {
    private final Long id;
    private final Long memberId;
    private final Slot slot;

    public static Reservation create(long memberId, Slot slot) {
        return new Reservation(null, memberId, slot);
    }

    public static Reservation of(Long id, Long memberId, Slot slot) {
        return new Reservation(id, memberId, slot);
    }

    public boolean isOwnedBy(Long memberId) {
        return Objects.equals(this.memberId, memberId);
    }

    public Long getSlotId() {
        return slot.getId();
    }
}
