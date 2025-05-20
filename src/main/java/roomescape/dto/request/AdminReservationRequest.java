package roomescape.dto.request;

import java.time.LocalDate;

public record AdminReservationRequest(
        Long memberId,
        Long themeId,
        LocalDate date,
        Long timeId
) {

}
