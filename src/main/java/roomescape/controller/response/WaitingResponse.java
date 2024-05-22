package roomescape.controller.response;

import roomescape.model.ReservationTime;
import roomescape.model.Waiting;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;

import java.time.LocalDate;

public class WaitingResponse {

    private final long id;
    private final LocalDate date;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;
    private final MemberResponse member;

    private WaitingResponse(long id, LocalDate date, ReservationTimeResponse time, ThemeResponse theme, MemberResponse member) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public static WaitingResponse from(Waiting waiting) {
        ReservationTime time = waiting.getTime();
        Theme theme = waiting.getTheme();
        Member member = waiting.getMember();
        return new WaitingResponse(waiting.getId(), waiting.getDate(),
                ReservationTimeResponse.from(time),
                ThemeResponse.from(theme),
                MemberResponse.from(member));
    }

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTimeResponse getTime() {
        return time;
    }

    public ThemeResponse getTheme() {
        return theme;
    }

    public MemberResponse getMember() {
        return member;
    }
}
