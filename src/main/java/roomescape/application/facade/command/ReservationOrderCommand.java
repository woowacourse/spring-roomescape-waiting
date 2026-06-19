package roomescape.application.facade.command;

import java.time.LocalDate;
import roomescape.application.service.command.ReservationPendingCommand;
import roomescape.domain.ReservationStatus;
import roomescape.domain.order.OrderType;

public record ReservationOrderCommand(
        String name,
        LocalDate date,
        Long themeId,
        Long timeId,
        OrderType orderType
) {

    public ReservationPendingCommand toReservationPendingCommand() {
        return new ReservationPendingCommand(
                name,
                date,
                themeId,
                timeId,
                resolveStatus()
        );
    }

    private ReservationStatus resolveStatus() {
        return switch (orderType) {
            case RESERVATION -> ReservationStatus.RESERVED;
            case WAITING -> ReservationStatus.WAITING;
        };
    }
}
