package roomescape.wating.repository.entity;

import java.sql.Date;
import java.time.LocalDateTime;

public record WaitingEntity(
        Long id,
        String customerName,
        Date reservationDate,
        LocalDateTime createdAt,
        Long timeId,
        Long themeId
) {
}
