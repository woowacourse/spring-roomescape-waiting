package roomescape.reservation.application.dto;

import lombok.Builder;
import roomescape.reservation.domain.Status;

@Builder
public record ReservationCancelCommand(
        String name,
        Status status
) {
}
