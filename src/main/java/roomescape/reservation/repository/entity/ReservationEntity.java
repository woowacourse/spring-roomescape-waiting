package roomescape.reservation.repository.entity;

import java.sql.Date;

public record ReservationEntity(
        Long id,
        String name,
        Date date,
        Long timeId,
        Long themeId
) {
}
