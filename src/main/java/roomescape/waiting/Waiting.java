package roomescape.waiting;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Waiting {
    private final Long id;
    private final Long memberId;
    private Long slotId;

    public static Waiting create(long memberId, long slotId) {
        return new Waiting(null, memberId, slotId);
    }

    public static Waiting of(Long id, Long memberId, Long slotId) {
        return new Waiting(id, memberId, slotId);
    }

    public boolean isOwnedBy(Long memberId) {
        return Objects.equals(this.memberId, memberId);
    }
}
