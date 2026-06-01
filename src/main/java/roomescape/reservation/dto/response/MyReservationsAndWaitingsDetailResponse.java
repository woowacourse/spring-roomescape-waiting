package roomescape.reservation.dto.response;

import roomescape.reservation.ReservationStatus;
import roomescape.reservation.application.readmodel.ReservationReadModel;
import roomescape.reservationtime.dto.response.TimeInformation;
import roomescape.theme.dto.response.ThemeFindResponse;
import roomescape.waiting.application.readmodel.WaitingReadModel;

import java.time.LocalDate;
import java.util.List;

public record MyReservationsAndWaitingsDetailResponse(
        Long id,
        String memberName,
        LocalDate date,
        ThemeFindResponse theme,
        TimeInformation time,
        ReservationStatus status,
        Long waitingOrder
) {
    public static List<MyReservationsAndWaitingsDetailResponse> from(List<ReservationReadModel> reservationReadModels) {
        return reservationReadModels.stream()
                .map(MyReservationsAndWaitingsDetailResponse::from)
                .toList();
    }

    public static MyReservationsAndWaitingsDetailResponse from(ReservationReadModel reservationReadModel) {
        return new MyReservationsAndWaitingsDetailResponse(
                reservationReadModel.id(),
                reservationReadModel.memberName(),
                reservationReadModel.date(),
                new ThemeFindResponse(
                        reservationReadModel.themeId(),
                        reservationReadModel.themeName(),
                        reservationReadModel.themeDescription(),
                        reservationReadModel.themeThumbnailUrl()
                ),
                new TimeInformation(
                        reservationReadModel.timeId(),
                        reservationReadModel.startAt()
                ),
                ReservationStatus.RESERVED,
                null
        );
    }

    public static MyReservationsAndWaitingsDetailResponse from(WaitingReadModel waitingReadModel) {
        return new MyReservationsAndWaitingsDetailResponse(
                waitingReadModel.id(),
                waitingReadModel.memberName(),
                waitingReadModel.date(),
                new ThemeFindResponse(
                        waitingReadModel.themeId(),
                        waitingReadModel.themeName(),
                        waitingReadModel.themeDescription(),
                        waitingReadModel.themeThumbnailUrl()
                ),
                new TimeInformation(
                        waitingReadModel.timeId(),
                        waitingReadModel.startAt()
                ),
                ReservationStatus.WAITING,
                waitingReadModel.waitingOrder()
        );
    }
}
