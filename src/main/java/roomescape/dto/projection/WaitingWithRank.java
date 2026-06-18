package roomescape.dto.projection;

import roomescape.domain.Waiting;

public record WaitingWithRank(Waiting waiting, Long rank) {

    public Long getId() {
        return waiting.getId();
    }

    public Long getRank() {
        return rank;
    }
}
