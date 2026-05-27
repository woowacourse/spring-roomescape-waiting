package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.theme.application.dto.ThemeResult;

public record WaitingResult(
        Long id,
        String name,
        LocalDate date,
        ThemeResult theme,
        ReservationTimeResult time,
        Long rank
) {

    public static WaitingResult from(Waiting waiting, ThemeResult themeResult,
                                     ReservationTimeResult timeResult,
                                     Long rank) {
        return new WaitingResult(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate(),
                themeResult,
                timeResult,
                rank
        );
    }
}
