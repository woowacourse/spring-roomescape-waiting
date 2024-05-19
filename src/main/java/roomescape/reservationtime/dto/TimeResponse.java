package roomescape.reservationtime.dto;

import java.time.LocalTime;

public record TimeResponse(long id, LocalTime startAt) {
}
