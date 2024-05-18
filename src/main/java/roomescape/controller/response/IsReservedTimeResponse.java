package roomescape.controller.response;

import java.time.LocalTime;

public record IsReservedTimeResponse(long timeId, LocalTime startAt, boolean alreadyBooked) {

}
