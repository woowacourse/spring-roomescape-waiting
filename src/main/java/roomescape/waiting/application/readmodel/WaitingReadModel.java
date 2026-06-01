package roomescape.waiting.application.readmodel;

import roomescape.waiting.infrastructure.projection.WaitingDetailProjection;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record WaitingReadModel(
        Long id,
        String memberName,
        LocalDate date,
        Long themeId,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        Long timeId,
        LocalTime startAt,
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
                projection.themeId(),
                projection.themeName(),
                projection.themeDescription(),
                projection.thumbnailUrl(),
                projection.timeId(),
                projection.startAt(),
                projection.waitingOrder()
        );
    }
}
