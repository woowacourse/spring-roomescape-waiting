package roomescape.service.dto.command;

import java.time.LocalDate;
import roomescape.controller.dto.request.ReservationRequest;

public record ReservationCommand(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
    public static ReservationCommand from(ReservationRequest request) {
        return new ReservationCommand(request.name(), request.date(), request.timeId(), request.themeId());
    }
}
