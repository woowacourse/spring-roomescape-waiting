package roomescape.dto.response;

import roomescape.domain.Waiting;

public record WaitingWithRankDto(
        Waiting waiting,
        Long rank) {
}
