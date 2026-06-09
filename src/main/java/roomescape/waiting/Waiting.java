package roomescape.waiting;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Waiting {
    private final Long id;
    private final Long memberId;
    private Long scheduleId;

    public boolean isSameMemberId(long otherMemberId) {
        return this.memberId.equals(otherMemberId);
    }
}

