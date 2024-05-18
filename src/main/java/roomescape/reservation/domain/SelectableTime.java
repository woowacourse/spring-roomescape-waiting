package roomescape.reservation.domain;

import java.time.LocalTime;

public record SelectableTime(long timeId, LocalTime startAt, boolean alreadyBooked) {

}
