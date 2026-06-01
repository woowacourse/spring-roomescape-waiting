package roomescape.reservation.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.repository.dto.WaitingOrderDetail;

public record WaitingQueryResult(Long id, String name, @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                 Long themeId, String themeName, Long timeId, LocalTime startAt, Long order) {

    public static WaitingQueryResult from(WaitingOrderDetail waitingOrderDetail) {
        return new WaitingQueryResult(waitingOrderDetail.waitingId(),
                waitingOrderDetail.username(),
                waitingOrderDetail.date(),
                waitingOrderDetail.themeId(),
                waitingOrderDetail.themeName(),
                waitingOrderDetail.timeId(),
                waitingOrderDetail.startAt(),
                waitingOrderDetail.order());
    }
}
