package roomescape.domain.waiting;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WaitingWithRank that = (WaitingWithRank) o;
        return Objects.equals(waiting, that.waiting) && Objects.equals(rank, that.rank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(waiting, rank);
    }
}
