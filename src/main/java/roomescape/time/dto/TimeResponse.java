package roomescape.time.dto;

import java.time.LocalTime;

public record TimeResponse(long id, LocalTime startAt) {
}
