package roomescape.waiting.infra.projection;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingDetailProjection(
        Long id,
        String memberName,
        LocalDate date,
        Long themeId,
        String themeName,
        String themeDescription,
        String thumbnailUrl,
        Long timeId,
        LocalTime startAt,
        Long waitingOrder
) {
}
