package roomescape.reservation.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.theme.domain.Theme;
import roomescape.timeslot.domain.ReservationTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@Getter
@ToString
public class ReservationSlot {

    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;

    public static ReservationSlot of(final ReservationDate date, final ReservationTime time, final Theme theme) {
        return new ReservationSlot(date, time, theme);
    }
}
