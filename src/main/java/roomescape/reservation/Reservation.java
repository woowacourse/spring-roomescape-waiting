package roomescape.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Reservation {
    private final Long id;
    private final Long memberId;
    private Long scheduleId;

    public static Reservation of(long id, long memberId, long scheduleId) {
        return new Reservation(id, memberId, scheduleId);
    }

    public boolean isSameMemberId(long otherMemberId) {
        return this.memberId.equals(otherMemberId);
    }

    public boolean isSameScheduleId(long newScheduleId) {
        return this.scheduleId.equals(newScheduleId);
    }
}
