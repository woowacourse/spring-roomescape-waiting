package roomescape.dao.dto;

import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;

public record WaitingWithRank(
        Long id,
        String name,
        LocalDate reservationDate,
        ReservationTime reservationTime,
        Theme reservationTheme,
        int rank) {
}
