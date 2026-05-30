package roomescape.reservation.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationSlot {

    private Long id;
    private Long dateId;
    private Long timeId;
    private Long themeId;

    public static ReservationSlot create(Long dateId, Long timeId, Long themeId) {
        return new ReservationSlot(null, dateId, timeId, themeId);
    }

    public ReservationSlot withId(Long id) {
        return new ReservationSlot(id, this.dateId, this.timeId, this.themeId);
    }
}
