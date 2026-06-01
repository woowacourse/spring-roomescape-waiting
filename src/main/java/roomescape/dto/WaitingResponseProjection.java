package roomescape.dto;

import roomescape.domain.Reservation;
import roomescape.domain.Store;
import roomescape.domain.Theme;

import java.time.LocalDateTime;

public record WaitingResponseProjection(
        Long order,
        Reservation reservation,
        Theme theme,
        Store store,
        Long memberId,
        LocalDateTime createdAt
) {
}
