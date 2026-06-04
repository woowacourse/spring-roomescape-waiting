package roomescape.waiting;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.slot.Slot;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Waiting {
    private final Long id;
    private final Long memberId;
    private final Long slotId;

    public void validateOwnedBy(long memberId) {
        if (!isOwnedBy(memberId)) {
            throw new EscapeRoomException(ErrorCode.WAITING_NOT_OWNED_BY_MEMBER, id);
        }
    }

    public static Waiting create(long memberId, long slotId) {
        return new Waiting(null, memberId, slotId);
    }

    public static Waiting of(Long id, Long memberId, Long slotId) {
        return new Waiting(id, memberId, slotId);
    }

    public boolean isOwnedBy(Long memberId) {
        return Objects.equals(this.memberId, memberId);
    }

    public boolean isFor(Slot slot) {
        Objects.requireNonNull(slot, "slot은 null일 수 없습니다.");
        return Objects.equals(this.slotId, slot.getId());
    }
}
