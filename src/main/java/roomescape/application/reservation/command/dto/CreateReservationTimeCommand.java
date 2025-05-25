package roomescape.application.reservation.command.dto;

import java.time.LocalTime;

public record CreateReservationTimeCommand(
        LocalTime startAt
) {
}
