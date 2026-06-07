package roomescape.dto;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Store;
import roomescape.domain.Theme;

public record StoreReservationResult(
        Reservation reservation,
        Theme theme,
        Store store,
        Member member
) {
}
