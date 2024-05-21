package roomescape.domain;

import roomescape.entity.Reservation;

public record Waiting(Reservation reservation, long rank) {
}
