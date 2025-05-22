package roomescape.domain.waiting;

import java.time.LocalDate;
import java.time.LocalTime;

public class WaitingWithRank {

    private Waiting waiting;
    private Long rank;

    public WaitingWithRank(Waiting waiting, long rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public Long getRank() {
        return rank;
    }

    public Long getWaitingId() {
        return waiting.getId();
    }

    public String getName() {
        return waiting.getName();
    }

    public String getThemeName() {
        return waiting.getThemeName();
    }

    public LocalDate getDate() {
        return waiting.getDate();
    }

    public LocalTime getStartAt() {
        return waiting.getStartAt();
    }
}
