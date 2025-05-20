package roomescape.reservation.model.repository.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationWithMember(
        Long id,
        LocalDate date,
        Long timeId,
        LocalTime startAt,
        Long themeId,
        String themeName,
        String themeDescription,
        String themeThumbnail,
        Long memberId,
        String memberName,
        String memberEmail
) {

}
