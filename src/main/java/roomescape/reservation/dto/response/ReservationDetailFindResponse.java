package roomescape.reservation.dto.response;

import roomescape.reservation.ReservationStatus;
import roomescape.reservation.application.readmodel.ReservationReadModel;
import roomescape.reservationtime.dto.response.TimeInformation;
import roomescape.theme.dto.response.ThemeFindResponse;
import roomescape.waiting.application.readmodel.WaitingReadModel;

import java.time.LocalDate;
import java.util.List;

public record ReservationDetailFindResponse(
        Long id,
        String memberName,
        LocalDate date,
        ThemeFindResponse theme,
        TimeInformation time,
        ReservationStatus status,
        Long waitingOrder
) {
    public static List<ReservationDetailFindResponse> from(List<ReservationReadModel> reservationReadModels) {
        return reservationReadModels.stream()
                .map(ReservationDetailFindResponse::from)
                .toList();
    }

    public static ReservationDetailFindResponse from(ReservationReadModel reservationReadModel) {
        return new ReservationDetailFindResponse(
                reservationReadModel.id(),
                reservationReadModel.memberName(),
                reservationReadModel.date(),
                reservationReadModel.theme(),
                reservationReadModel.time(),
                ReservationStatus.RESERVED,
                null
        );
    }

    public static ReservationDetailFindResponse from(WaitingReadModel waitingReadModel) {
        return new ReservationDetailFindResponse(
                waitingReadModel.id(),
                waitingReadModel.memberName(),
                waitingReadModel.date(),
                waitingReadModel.theme(),
                waitingReadModel.time(),
                ReservationStatus.WAITING,
                waitingReadModel.waitingOrder()
        );
    }
}
