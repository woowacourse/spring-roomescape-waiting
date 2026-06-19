package roomescape.reservation.event.schema;

import java.time.LocalDate;
import roomescape.reservation.domain.PromotionSource;

public record PromotionFailed(
        LocalDate date,
        Long themeId,
        Long timeId,
        int retryCount,
        PromotionSource source
) {
}
