package roomescape.reservation.dto;

import java.time.LocalDate;

public record ReservationSimpleInfo(
        LocalDate date,
        String memberName,
        long memberId,
        long timeId,
        long themeId
) {

}
