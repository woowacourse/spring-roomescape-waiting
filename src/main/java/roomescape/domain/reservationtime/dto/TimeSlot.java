package roomescape.domain.reservationtime.dto;

import java.time.LocalDate;

public record TimeSlot(
    LocalDate date,
    Long timeId,
    Long themeId
) {

}
