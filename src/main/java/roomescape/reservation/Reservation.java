package roomescape.reservation;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.slot.Slot;

@Getter
public class Reservation {
    private final Long id;
    private final Long memberId;
    private final Slot slot;

    private Reservation(Long id, Long memberId, Slot slot) {
        this.id = id;
        this.memberId = Objects.requireNonNull(memberId, "memberId는 null일 수 없습니다.");
        this.slot = Objects.requireNonNull(slot, "slot은 null일 수 없습니다.");
    }

    public static Reservation create(long memberId, Slot slot) {
        return new Reservation(null, memberId, slot);
    }

    public static Reservation of(Long id, Long memberId, Slot slot) {
        return new Reservation(id, memberId, slot);
    }

    public boolean isOwnedBy(Long memberId) {
        return Objects.equals(this.memberId, memberId);
    }

    public void validateOwnedBy(Long memberId) {
        if (!isOwnedBy(memberId)) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_NOT_OWNED_BY_MEMBER, id);
        }
    }

    public void validateNotPast(LocalDateTime now) {
        slot.validateNotPast(now);
    }

    public Long getSlotId() {
        return slot.getId();
    }
}
