package roomescape.reservation.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.dto.ReservationDetail;
import roomescape.reservation.domain.repository.dto.WaitingOrderDetail;
import roomescape.reservationtime.application.dto.ReservationTimeQueryResult;
import roomescape.theme.application.dto.ThemeQueryResult;

public record WaitingQueryResult(Long id, String name, @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                 ThemeQueryResult theme, ReservationTimeQueryResult time, Long order) {

    public static WaitingQueryResult from(Waiting waiting, ThemeQueryResult themeQueryResult,
                                          ReservationTimeQueryResult timeQueryResult,
                                          Long order) {
        return new WaitingQueryResult(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate(),
                themeQueryResult,
                timeQueryResult,
                order
        );
    }

    public static WaitingQueryResult from(WaitingOrderDetail waitingOrderDetail, ThemeQueryResult themeQueryResult,
                                          ReservationTimeQueryResult timeQueryResult) {
        return new WaitingQueryResult(waitingOrderDetail.waitingId(), waitingOrderDetail.themeName())
    }

    public static

}
