package roomescape.reservation.event.schema;

import java.time.LocalDate;
import roomescape.reservation.domain.PromotionSource;

public record WaitingPromotedToReservation(
        LocalDate date,
        Long themeId,
        Long timeId,
        PromotionSource source
) {
}
