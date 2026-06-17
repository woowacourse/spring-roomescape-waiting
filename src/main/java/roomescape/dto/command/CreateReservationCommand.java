package roomescape.dto.command;

import java.time.LocalDate;
import roomescape.dto.request.CreatePaymentReservationRequest;
import roomescape.dto.request.CreateReservationRequest;

public record CreateReservationCommand(
        Long userId,
        Long themeId,
        LocalDate date,
        Long timeId,
        Long storeId,
        Long amount
) {
    public static CreateReservationCommand of(Long userId, CreateReservationRequest request) {
        return new CreateReservationCommand(userId, request.themeId(), request.date(), request.timeId(),
                request.storeId(), null);
    }

    public static CreateReservationCommand of(Long userId, CreatePaymentReservationRequest request) {
        return new CreateReservationCommand(userId, request.themeId(), request.date(), request.timeId(),
                request.storeId(), request.amount());
    }
}
