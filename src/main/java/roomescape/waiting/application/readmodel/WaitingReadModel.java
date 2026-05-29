package roomescape.waiting.application.readmodel;

import roomescape.reservation.ReservationStatus;
import roomescape.reservationtime.dto.response.TimeInformation;
import roomescape.theme.dto.response.ThemeFindResponse;
import roomescape.waiting.infrastructure.projection.WaitingDetailProjection;

import java.time.LocalDate;
import java.util.List;

public record WaitingReadModel(
        Long id,
        String memberName,
        LocalDate date,
        ThemeFindResponse theme,
        TimeInformation time,
        ReservationStatus status,
        Long waitingOrder
) {
    public static List<WaitingReadModel> from(List<WaitingDetailProjection> projections) {
        return projections.stream()
                .map(WaitingReadModel::from)
                .toList();
    }

    public static WaitingReadModel from(WaitingDetailProjection projection) {
        return new WaitingReadModel(
                projection.id(),
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
                ReservationStatus.WAITING,
                projection.waitingOrder()
        );
    }
}
