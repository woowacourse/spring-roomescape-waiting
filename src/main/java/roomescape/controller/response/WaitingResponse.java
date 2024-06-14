package roomescape.controller.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.model.Waiting;

public class WaitingResponse {

    private final long id;
    private final LocalDate date;
    private final LocalDateTime created_at;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;
    private final MemberResponse member;

    private WaitingResponse(long id, LocalDate date, LocalDateTime created_at, ReservationTimeResponse time,
                            ThemeResponse theme, MemberResponse member) {
        this.id = id;
        this.date = date;
        this.created_at = created_at;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public WaitingResponse(Waiting waiting) {
        this(waiting.getId(),
                waiting.getDate(),
                waiting.getCreated_at(),
                new ReservationTimeResponse(waiting.getTime()),
                new ThemeResponse(waiting.getTheme()),
                new MemberResponse(waiting.getMember()));
    }

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
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
