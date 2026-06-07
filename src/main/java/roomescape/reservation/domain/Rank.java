package roomescape.reservation.domain;

import lombok.Builder;
import roomescape.global.exception.RoomEscapeException;

public record Rank(int value) {

    @Builder
    public Rank {
        validateValue(value);
    }

    private static void validateValue(int value) {
        if (value <= 0) {
            throw new RoomEscapeException("대기 순번은 양수여야 합니다.");
        }
    }

    public Rank postpone(int steps, int totalRankCount) {
        validatePostponeSteps(steps);
        validateNotLastRank(totalRankCount);

        return Rank.builder()
                .value(Math.min(value + steps, totalRankCount))
                .build();
    }

    private void validatePostponeSteps(int steps) {
        if (steps <= 0) {
            throw new RoomEscapeException("미룰 순번은 양수여야 합니다.");
        }
    }

    private void validateNotLastRank(int totalRankCount) {
        if (value == totalRankCount) {
            throw new RoomEscapeException("마지막 대기 순번은 대기를 미룰 수 없습니다.");
        }
    }
}
