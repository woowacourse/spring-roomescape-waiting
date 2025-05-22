package roomescape.waiting.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.theme.domain.ThemeName;

public class WaitingWithRank {

    private Waiting waiting;
    private long rank;

    public WaitingWithRank(final Waiting waiting, final long rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    public Waiting getWaiting() {
        return waiting;
    }

    public long getRank() {
        return rank;
    }

    public ThemeName getThemeName() {
        return waiting.getThemeName();
    }

    public LocalDate getDate() {
        return waiting.getDate();
    }

    public LocalTime getStartAt() {
        return waiting.getStartAt();
    }
}
