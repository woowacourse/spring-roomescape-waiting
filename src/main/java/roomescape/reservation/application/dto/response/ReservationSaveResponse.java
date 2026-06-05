package roomescape.reservation.application.dto.response;

import roomescape.reservation.domain.Reservation;

public record ReservationSaveResponse(
        Long id,
        Long memberId,
        Long slotId
) {
    public static ReservationSaveResponse from(Reservation reservation) {
        return new ReservationSaveResponse(
                reservation.getId(),
                reservation.getMemberId(),
                reservation.getSlotId()
        );
    }
}
