package roomescape.dto.result;

import roomescape.domain.Reservation;
import roomescape.domain.Store;
import roomescape.domain.Theme;
import roomescape.dto.projection.MemberSummaryProjection;

public record StoreReservationResult(
        Reservation reservation,
        Theme theme,
        Store store,
        MemberSummaryProjection member
) {
}
