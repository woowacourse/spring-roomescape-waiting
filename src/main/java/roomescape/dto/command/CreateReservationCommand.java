package roomescape.dto.command;

import java.time.LocalDate;
import roomescape.dto.request.CreateReservationRequest;

public record CreateReservationCommand(
        Long userId,
        Long themeId,
        LocalDate date,
        Long timeId,
        Long storeId
) {
    public static CreateReservationCommand of(Long userId, CreateReservationRequest request) {
        return new CreateReservationCommand(userId, request.themeId(), request.date(), request.timeId(),
                request.storeId());
    }
}
