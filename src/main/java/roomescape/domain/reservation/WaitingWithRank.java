package roomescape.domain.reservation;

import java.util.Objects;

public record WaitingWithRank(
        Waiting waiting,
        long rank
) {

    public WaitingWithRank {
        Objects.requireNonNull(waiting, "waiting은 null일 수 없습니다.");
    }
}
