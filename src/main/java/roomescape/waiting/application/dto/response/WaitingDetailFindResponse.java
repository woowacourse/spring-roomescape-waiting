package roomescape.waiting.application.dto.response;

import java.time.LocalDate;
import roomescape.reservationtime.application.dto.response.TimeInformation;
import roomescape.theme.application.dto.response.ThemeFindResponse;
import roomescape.waiting.application.port.out.projection.WaitingDetailProjection;

public record WaitingDetailFindResponse(
        Long id,
        Long slotId,
        String memberName,
        LocalDate date,
        ThemeFindResponse theme,
        TimeInformation time,
        Long waitingOrder
) {
    public static WaitingDetailFindResponse from(WaitingDetailProjection projection, long waitingOrder) {
        return new WaitingDetailFindResponse(
                projection.id(),
                projection.slotId(),
                projection.memberName(),
                projection.date(),
                new ThemeFindResponse(
                        projection.themeId(),
                        projection.themeName(),
                        projection.themeDescription(),
                        projection.thumbnailUrl()
                ),
                new TimeInformation(
                        projection.timeId(),
                        projection.startAt()
                ),
                waitingOrder
        );
    }
}
