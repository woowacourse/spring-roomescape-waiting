package roomescape.reservation.event.schema;

import java.time.LocalDate;

public record PromotionFailed(
        LocalDate date,
        Long themeId,
        Long timeId,
        int retryCount
) {
}
