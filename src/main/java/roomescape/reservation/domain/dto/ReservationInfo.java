package roomescape.reservation.domain.dto;

import java.time.LocalDate;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public record ReservationInfo(LocalDate date, ReservationTime time, Theme theme) {
}
