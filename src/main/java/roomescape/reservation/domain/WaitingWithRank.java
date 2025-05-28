package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public class WaitingWithRank {

    private final Waiting waiting;
    private final Long rank;

    public WaitingWithRank(final Waiting waiting, final Long rank) {
        this.waiting = waiting;
        this.rank = rank;
    }

    public Long getId() {
        return waiting.getId();
    }

    public String themeName() {
        return waiting.themeName();
    }

    public LocalDate getDate() {
        return waiting.getDate();
    }

    public LocalTime startTime() {
        return waiting.startTime();
    }

    public Long getRank() {
        return rank;
    }
}
