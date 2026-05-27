package roomescape.reservation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.reservation.application.dto.WaitingQueryResult;
import roomescape.reservationtime.application.dto.ReservationTimeQueryResult;
import roomescape.theme.application.dto.ThemeQueryResult;

public record WaitingResponse(
        Long id, String name, @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        ThemeQueryResult theme, ReservationTimeQueryResult time, Long order

) {

    public static WaitingResponse from(WaitingQueryResult waitingQueryResult) {
        return new WaitingResponse(waitingQueryResult.id(),
                waitingQueryResult.name(),
                waitingQueryResult.date(),
                waitingQueryResult.theme(),
                waitingQueryResult.time(),
                waitingQueryResult.order());
    }
}
