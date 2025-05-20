package roomescape.presentation.dto.response;

import roomescape.business.model.entity.Reservation;

public record ReservationWithAhead(
        Reservation reservation,
        long aheadCount
) {}
