package roomescape.controller.admin.dto.response;

import java.time.LocalDate;
import roomescape.service.result.ReservationResult;

public record AdminReservationResponse(
        long reservationId,
        LocalDate date,
        AdminThemeResponse theme,
        AdminReservationTimeResponse time,
        AdminReservationEntryResponse entry
) {

    public static AdminReservationResponse from(ReservationResult result) {
        return new AdminReservationResponse(
                result.reservationId(),
                result.date(),
                AdminThemeResponse.from(result.theme()),
                AdminReservationTimeResponse.from(result.time()),
                AdminReservationEntryResponse.from(result.entry())
        );
    }
}
