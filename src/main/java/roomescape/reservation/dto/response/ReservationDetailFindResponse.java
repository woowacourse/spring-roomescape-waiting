package roomescape.reservation.dto.response;

import roomescape.reservation.infrastructure.projection.ReservationDetailProjection;
import roomescape.reservationtime.dto.response.TimeInformation;
import roomescape.theme.dto.response.ThemeFindResponse;

import java.time.LocalDate;
import java.util.List;

public record ReservationDetailFindResponse(
        Long id,
        String memberName,
        LocalDate date,
        ThemeFindResponse theme,
        TimeInformation time
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
                        projection.thumbnailUrl()
                ),
                new TimeInformation(
                        projection.timeId(),
                        projection.startAt()
                )
        );
    }
}
