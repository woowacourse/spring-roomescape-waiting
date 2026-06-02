package roomescape.dto;

import roomescape.domain.Reservation;
import roomescape.domain.Store;
import roomescape.domain.Theme;

public record ReservationResult(
        Reservation reservation,
        Theme theme,
        Store store
) {
}
