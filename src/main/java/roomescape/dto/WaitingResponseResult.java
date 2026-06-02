package roomescape.dto;

import roomescape.domain.Reservation;
import roomescape.domain.Store;
import roomescape.domain.Theme;

import java.time.LocalDateTime;

public record WaitingResponseResult(
        Long order,
        Reservation reservation,
        Theme theme,
        Store store,
        Long memberId,
        LocalDateTime createdAt
) {
    public static WaitingResponseResult from(WaitingResponseProjection projection) {
        return new WaitingResponseResult(
                projection.order(),
                projection.reservation(),
                projection.theme(),
                projection.store(),
                projection.memberId(),
                projection.createdAt()
        );
    }
}
