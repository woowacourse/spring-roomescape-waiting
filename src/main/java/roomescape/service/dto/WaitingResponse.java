package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.Waiting;

public class WaitingResponse {

    private final Long id;
    private final LocalDate date;
    private final MemberResponse member;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;

    public WaitingResponse(Long id, LocalDate date, MemberResponse member, ReservationTimeResponse time,
                           ThemeResponse theme) {
        this.id = id;
        this.date = date;
        this.member = member;
        this.time = time;
        this.theme = theme;
    }

    public WaitingResponse(Waiting waiting) {
        this(waiting.getId(),
                waiting.getDate(),
                new MemberResponse(waiting.getMember()),
                new ReservationTimeResponse(waiting.getTime()),
                new ThemeResponse(waiting.getTheme()));
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public MemberResponse getMember() {
        return member;
    }

    public ReservationTimeResponse getTime() {
        return time;
    }

    public ThemeResponse getTheme() {
        return theme;
    }
}
