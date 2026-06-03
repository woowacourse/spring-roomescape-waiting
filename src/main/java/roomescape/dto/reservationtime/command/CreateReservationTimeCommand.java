package roomescape.dto.reservationtime.command;

import java.time.LocalTime;
import roomescape.dto.reservationtime.request.CreateReservationTimeRequest;

public record CreateReservationTimeCommand(
        LocalTime startAt
) {
    public static CreateReservationTimeCommand from(CreateReservationTimeRequest request) {
        return new CreateReservationTimeCommand(request.startAt());
    }
}