package roomescape.reservation.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import roomescape.theme.domain.ThemeId;
import roomescape.timeslot.domain.ReservationTime;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class ReservationSlot {

    private final ReservationDate date;
    private final ReservationTime time;
    private final ThemeId themeId;

    public static ReservationSlot from(final Reservation reservation) {
        return new ReservationSlot(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme().getId());
    }

    public boolean isSame(final ReservationSlot other) {
        return this.equals(other);
    }
}
