package roomescape.waiting.dto;

import roomescape.waiting.domain.Waiting;

public record WaitingWithRank(Waiting waiting, long rank) {
}
