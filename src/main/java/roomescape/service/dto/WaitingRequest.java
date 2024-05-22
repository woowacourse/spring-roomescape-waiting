package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

public class WaitingRequest {

    private final LocalDate date;
    private final Long time;
    private final Long theme;

    public WaitingRequest(LocalDate date, Long time, Long theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Waiting toWaiting(Member member, ReservationTime time, Theme theme) {
        return new Waiting(date, member, time, theme);
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getTime() {
        return time;
    }

    public Long getTheme() {
        return theme;
    }
}
