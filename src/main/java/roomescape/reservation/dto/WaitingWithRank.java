package roomescape.reservation.dto;

import roomescape.reservation.model.Waiting;

public record WaitingWithRank(Waiting waiting, long rank) {
}
