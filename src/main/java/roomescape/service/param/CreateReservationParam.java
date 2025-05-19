package roomescape.service.param;

import roomescape.controller.request.CreateReservationAdminRequest;

import java.time.LocalDate;

public record CreateReservationParam(
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId
) {

    public static CreateReservationParam from(CreateReservationAdminRequest reservationAdminRequest) {
        return new CreateReservationParam(
                reservationAdminRequest.memberId(),
                reservationAdminRequest.date(),
                reservationAdminRequest.timeId(),
                reservationAdminRequest.themeId()
        );
    }
}
