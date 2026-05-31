package roomescape.reservationslot.repository.entity;

import java.sql.Date;

public record ReservationSlotEntity(
        Long id,
        Date date,
        Long timeId,
        Long themeId
) {
}
