package roomescape.reservation.vo;

import roomescape.reservation.domain.Waiting;

public record WaitingWithRank(Waiting waiting, Long rank) {
}
