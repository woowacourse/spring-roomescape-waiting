package roomescape.business.dto;

import roomescape.business.model.entity.Reservation;

public record ReservationWithAheadDto(
        Reservation reservation,
        Long aheadCount
) {
}
