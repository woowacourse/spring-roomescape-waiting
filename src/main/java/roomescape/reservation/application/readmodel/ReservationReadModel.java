package roomescape.reservation.application.readmodel;

import roomescape.reservation.infrastructure.projection.ReservationDetailProjection;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReservationReadModel(
        Long id,
        String memberName,
        LocalDate date,
        Long themeId,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        Long timeId,
        LocalTime startAt
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
                projection.themeId(),
                projection.themeName(),
                projection.themeDescription(),
                projection.thumbnailUrl(),
                projection.timeId(),
                projection.startAt()
        );
    }
}
