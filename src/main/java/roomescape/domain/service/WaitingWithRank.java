package roomescape.domain.service;

import roomescape.domain.common.UserName;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationWaiting;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;

public record WaitingWithRank(
        Long id,
        UserName name,
        LocalDate reservationDate,
        ReservationTime reservationTime,
        Theme reservationTheme,
        int rank
) {
    public static WaitingWithRank withRank(ReservationWaiting waiting, int rank) {
        return new WaitingWithRank(
                waiting.getId(),
                waiting.getUserName(),
                waiting.getWaitingDate(),
                waiting.getWaitingTime(),
                waiting.getWaitingTheme(),
                rank
                );
    }
}
