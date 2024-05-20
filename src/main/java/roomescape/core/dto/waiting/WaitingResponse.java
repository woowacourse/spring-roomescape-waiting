package roomescape.core.dto.waiting;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import roomescape.core.domain.Member;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Theme;
import roomescape.core.domain.Waiting;
import roomescape.core.dto.member.MemberResponse;
import roomescape.core.dto.reservationtime.ReservationTimeResponse;
import roomescape.core.dto.theme.ThemeResponse;

public class WaitingResponse {
    private final Long id;
    private final MemberResponse member;
    private final String date;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;

    public WaitingResponse(final Waiting waiting) {
        this(waiting.getId(), waiting.getMember(), waiting.getDate(), waiting.getTime(), waiting.getTheme());
    }

    public WaitingResponse(final Long id, final Member member, final LocalDate date, final ReservationTime time,
                           final Theme theme) {
        this.id = id;
        this.member = new MemberResponse(member);
        this.date = date.format(DateTimeFormatter.ISO_DATE);
        this.time = new ReservationTimeResponse(time);
        this.theme = new ThemeResponse(theme);
    }

    public Long getId() {
        return id;
    }

    public MemberResponse getMember() {
        return member;
    }

    public String getDate() {
        return date;
    }

    public ReservationTimeResponse getTime() {
        return time;
    }

    public ThemeResponse getTheme() {
        return theme;
    }
}
