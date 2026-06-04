package roomescape.reservation.application.dto;

import lombok.Builder;

@Builder
public record ReservationCancelCommand(
        String name
) {
}
