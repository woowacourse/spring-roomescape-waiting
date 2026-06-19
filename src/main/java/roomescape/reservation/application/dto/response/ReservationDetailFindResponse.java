package roomescape.reservation.application.dto.response;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.application.port.out.projection.ReservationDetailProjection;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservationtime.application.dto.response.TimeInformation;
import roomescape.theme.application.dto.response.ThemeFindResponse;
import roomescape.waiting.application.port.out.projection.WaitingDetailProjection;

public record ReservationDetailFindResponse(
        Long id,
        String memberName,
        LocalDate date,
        ThemeFindResponse theme,
        TimeInformation time,
        ReservationStatus status,
        Long waitingOrder
) {
    public static List<ReservationDetailFindResponse> from(List<ReservationDetailProjection> projections) {
        return projections.stream()
                .map(ReservationDetailFindResponse::from)
                .toList();
    }

    public static ReservationDetailFindResponse from(ReservationDetailProjection projection) {
        return new ReservationDetailFindResponse(
                projection.id(),
                projection.memberName(),
                projection.date(),
                new ThemeFindResponse(
                        projection.themeId(),
                        projection.themeName(),
                        projection.themeDescription(),
                        projection.thumbnailUrl(),
                        projection.themePrice()
                ),
                new TimeInformation(
                        projection.timeId(),
                        projection.startAt()
                ),
                projection.status(),
                null
        );
    }

    public static ReservationDetailFindResponse from(WaitingDetailProjection projection, long waitingOrder) {
        return new ReservationDetailFindResponse(
                projection.id(),
                projection.memberName(),
                projection.date(),
                new ThemeFindResponse(
                        projection.themeId(),
                        projection.themeName(),
                        projection.themeDescription(),
                        projection.thumbnailUrl(),
                        0
                ),
                new TimeInformation(
                        projection.timeId(),
                        projection.startAt()
                ),
                ReservationStatus.WAITING,
                waitingOrder
        );
    }

}
