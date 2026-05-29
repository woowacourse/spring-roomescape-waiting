package roomescape.reservation.application.readmodel;

import roomescape.reservation.ReservationStatus;
import roomescape.reservation.infrastructure.projection.ReservationDetailProjection;
import roomescape.reservationtime.dto.response.TimeInformation;
import roomescape.theme.dto.response.ThemeFindResponse;
import roomescape.waiting.infrastructure.projection.WaitingDetailProjection;

import java.time.LocalDate;
import java.util.List;

public record ReservationReadModel(
        Long id,
        String memberName,
        LocalDate date,
        ThemeFindResponse theme,
        TimeInformation time,
        ReservationStatus status,
        Long waitingOrder
) {
    public static List<ReservationReadModel> from(List<ReservationDetailProjection> projections) {
        return projections.stream()
                .map(ReservationReadModel::from)
                .toList();
    }

    public static ReservationReadModel from(ReservationDetailProjection projection) {
        return new ReservationReadModel(
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
                ReservationStatus.RESERVED,
                null
        );
    }
}
