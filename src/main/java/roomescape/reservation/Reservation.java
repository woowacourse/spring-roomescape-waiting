package roomescape.reservation;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Reservation {
    private final Long id;
    private final Long memberId;
    private Long slotId;

    public static Reservation create(long memberId, long slotId) {
        return new Reservation(null, memberId, slotId);
    }

    public static Reservation of(Long id, Long memberId, Long slotId) {
        return new Reservation(id, memberId, slotId);
    }

    public boolean isOwnedBy(Long memberId) {
        return Objects.equals(this.memberId, memberId);
    }
}
