package roomescape.service.result;

import roomescape.domain.WaitingWithRank;

import java.time.LocalDate;
import java.util.List;

public record WaitingWithRankResult(
        Long id,
        ThemeResult theme,
        LocalDate date,
        ReservationTimeResult time,
        long rank
) {
    public static WaitingWithRankResult from(WaitingWithRank waitingWithRank) {
        return new WaitingWithRankResult(
                waitingWithRank.waiting().getId(),
                ThemeResult.from(waitingWithRank.waiting().getTheme()),
                waitingWithRank.waiting().getDate(),
                ReservationTimeResult.from(waitingWithRank.waiting().getTime()),
                waitingWithRank.rank()
        );
    }

    public static List<WaitingWithRankResult> from(List<WaitingWithRank> waitingsWithRank) {
        return waitingsWithRank.stream()
                .map(WaitingWithRankResult::from)
                .toList();
    }
}
