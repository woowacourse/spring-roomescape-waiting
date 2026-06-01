package roomescape.reservation.presentation.dto;

import roomescape.reservation.application.dto.ReservationCancelCommand;
import roomescape.reservation.domain.Status;

public record ReservationCancelRequest(
        String username,
        Status status
) {
    public ReservationCancelCommand toCommand() {
        return ReservationCancelCommand.builder()
                .name(username)
                .status(status)
                .build();
    }
}
