package roomescape.common.domain;

import java.time.LocalDate;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public record ReservationSlot(LocalDate date, ReservationTime time, Theme theme) {
}
