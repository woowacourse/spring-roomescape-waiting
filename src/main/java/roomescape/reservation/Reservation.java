package roomescape.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Reservation {
    private final Long id;
    private final Long memberId;
    private Long slotId;

    public static Reservation of(long id, long memberId, long slotId) {
        return new Reservation(id, memberId, slotId);
    }
}
