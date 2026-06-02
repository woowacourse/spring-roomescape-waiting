package roomescape.dto.reservation.command;

import java.time.LocalDate;
import roomescape.domain.User;
import roomescape.dto.reservation.request.CreateReservationRequest;

public record CreateReservationCommand(
        User user,
        Long themeId,
        LocalDate date,
        Long timeId,
        Long storeId
) {
    public static CreateReservationCommand of(User user, CreateReservationRequest request) {
        return new CreateReservationCommand(user, request.themeId(), request.date(), request.timeId(),
                request.storeId());
    }
}
