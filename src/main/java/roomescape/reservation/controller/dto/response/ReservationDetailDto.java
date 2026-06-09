package roomescape.reservation.controller.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationDetailDto(
        Long id,
        Long slotId,
        String name,
        ReservationStatus status,
        LocalDateTime reservedAt
) {

    public static ReservationDetailDto from(Reservation reservation) {
        return new ReservationDetailDto(
                reservation.getId(),
                reservation.getSlotId(),
                reservation.getName(),
                reservation.getStatus(),
                reservation.getReservedAt()
        );
    }

}
