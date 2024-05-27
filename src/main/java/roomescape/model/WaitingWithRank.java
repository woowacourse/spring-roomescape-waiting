package roomescape.model;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import roomescape.model.theme.Theme;

public class WaitingWithRank {

    private final Waiting waiting;
    private final long rank;

    public WaitingWithRank(Waiting target, List<Waiting> allWaitings) {
        this.waiting = target;
        this.rank = calculateRank(target, allWaitings);
    }

    private long calculateRank(Waiting target, List<Waiting> allWaitings) {
        List<Waiting> waitings = allWaitings.stream()
                .filter(waiting -> target.getDate().isEqual(waiting.getDate()))
                .filter(waiting -> target.getTime().getStartAt() == waiting.getTime().getStartAt())
                .filter(waiting -> target.getTheme().getName().equals(waiting.getTheme().getName()))
                .sorted(Comparator.comparing(Waiting::getCreated_at))
                .toList();

        return waitings.indexOf(target) + 1;
    }

    public long getId() {
        return waiting.getId();
    }

    public Theme getTheme() {
        return waiting.getTheme();
    }

    public LocalDate getDate() {
        return waiting.getDate();
    }

    public ReservationTime getTime() {
        return waiting.getTime();
    }

    public long getRank() {
        return rank;
    }
}
