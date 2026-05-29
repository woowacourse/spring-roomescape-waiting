package roomescape.dao.dto;

import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;

public record WaitingWithRank(
        Long id,
        String name,
        LocalDate reservationDate,
        ReservationTime reservationTime,
        Theme reservationTheme,
        int rank) {
}
