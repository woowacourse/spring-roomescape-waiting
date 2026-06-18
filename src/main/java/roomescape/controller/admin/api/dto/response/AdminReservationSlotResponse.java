package roomescape.controller.admin.api.dto.response;

import java.time.LocalDate;
import roomescape.application.service.result.ReservationSlotResult;

public record AdminReservationSlotResponse(
        long slotId,
        LocalDate date,
        AdminThemeResponse theme,
        AdminReservationTimeResponse time,
        AdminReservationResponse reservation
) {

    public static AdminReservationSlotResponse from(ReservationSlotResult result) {
        return new AdminReservationSlotResponse(
                result.slotId(),
                result.date(),
                AdminThemeResponse.from(result.theme()),
                AdminReservationTimeResponse.from(result.time()),
                AdminReservationResponse.from(result.reservation())
        );
    }
}
