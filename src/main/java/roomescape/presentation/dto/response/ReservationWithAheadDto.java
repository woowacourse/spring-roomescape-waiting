package roomescape.presentation.dto.response;

import roomescape.business.model.entity.Reservation;

public record ReservationWithAheadDto(
        Reservation reservation,
        Long aheadCount
) {
}
