package roomescape.reservation.dto;

import java.time.LocalDate;

public record CreateReservationRequest(
        LocalDate date,
        Long themeId,
        Long timeId
) {

}
