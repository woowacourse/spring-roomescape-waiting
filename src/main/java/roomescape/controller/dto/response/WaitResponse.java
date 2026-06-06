package roomescape.controller.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.ReservationStatus;
import roomescape.service.dto.WaitInfo;

public record WaitResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        ReservationStatus status,
        Long order,
        LocalDateTime createdAt
) implements ReservationWaitResponse {

    public static WaitResponse from(WaitInfo waitInfo) {
        return new WaitResponse(
                waitInfo.id(),
                waitInfo.name(),
                waitInfo.date(),
                ReservationTimeResponse.from(waitInfo.time()),
                ThemeResponse.from(waitInfo.theme()),
                waitInfo.status(),
                waitInfo.order(),
                waitInfo.createdAt()
        );
    }
}
