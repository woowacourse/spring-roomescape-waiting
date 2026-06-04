package roomescape.reservation.dto.response;

import roomescape.reservation.ReservationStatus;
import roomescape.reservation.infrastructure.projection.ReservationDetailProjection;
import roomescape.reservationtime.dto.response.TimeInformation;
import roomescape.theme.dto.response.ThemeFindResponse;
import roomescape.waiting.infrastructure.projection.WaitingDetailProjection;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

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

    public static ReservationDetailFindResponse from(WaitingDetailProjection projection, long waitingOrder) {
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
                ),
                ReservationStatus.WAITING,
                waitingOrder
        );
    }

    public static List<ReservationDetailFindResponse> merge(
            List<ReservationDetailProjection> reservationProjections,
            List<WaitingDetailProjection> waitingProjections,
            Function<WaitingDetailProjection, Long> waitingOrderResolver
    ) {
        return Stream.concat(
                reservationProjections.stream().map(ReservationDetailFindResponse::from),
                waitingProjections.stream()
                        .map(projection -> ReservationDetailFindResponse.from(
                                projection,
                                waitingOrderResolver.apply(projection)
                        ))
        ).toList();
    }
}
