package roomescape.domain.service;

import org.springframework.stereotype.Component;
import roomescape.domain.reservation.ReservationWaiting;

import java.util.Comparator;
import java.util.List;

@Component
public class WaitingRanker {
    private static final Comparator<ReservationWaiting> WAITING_COMPARATOR =
            Comparator.comparing(ReservationWaiting::getCreatedAt);

    public ReservationWaiting getEarliestWaiting(List<ReservationWaiting> waitings) {
        return waitings.stream()
                .min(WAITING_COMPARATOR)
                .orElseThrow(() -> new IllegalArgumentException("빈 대기열이 입력되었습니다."));
    }

    public WaitingWithRank getWaitingWithRank(List<ReservationWaiting> allWaitingsInSlot, ReservationWaiting waiting) {
        int rank = allWaitingsInSlot.indexOf(waiting) + 1;

        return WaitingWithRank.withRank(waiting, rank);
    }
}
