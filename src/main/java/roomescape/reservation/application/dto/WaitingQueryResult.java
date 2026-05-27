package roomescape.reservation.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.reservation.domain.repository.dto.WaitingOrderDetail;
import roomescape.reservationtime.application.dto.ReservationTimeQueryResult;
import roomescape.theme.application.dto.ThemeQueryResult;

public record WaitingQueryResult(Long id, String name, @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                 ThemeQueryResult theme, ReservationTimeQueryResult time, Long order) {

    public static WaitingQueryResult from(WaitingOrderDetail waitingOrderDetail) {
        return new WaitingQueryResult(waitingOrderDetail.waitingId(),
                waitingOrderDetail.username(),
                waitingOrderDetail.date(),
                new ThemeQueryResult(waitingOrderDetail.themeId(), waitingOrderDetail.themeName(),
                        waitingOrderDetail.themeDescription(), waitingOrderDetail.thumbnailImgUrl()),
                new ReservationTimeQueryResult(waitingOrderDetail.timeId(), waitingOrderDetail.startAt()),
                waitingOrderDetail.order());
    }
}
