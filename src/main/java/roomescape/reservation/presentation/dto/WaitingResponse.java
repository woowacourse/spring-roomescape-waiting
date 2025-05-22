package roomescape.reservation.presentation.dto;

import java.time.LocalDate;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.reservation.domain.Waiting;

public class WaitingResponse {
    private Long id;
    private LocalDate date;
    private MemberResponse member;
    private ThemeResponse theme;
    private ReservationTimeResponse time;

    private WaitingResponse() {
    }

    public WaitingResponse(final Waiting waiting) {
        this.id = waiting.getId();
        this.date = waiting.getDate();
        this.member = new MemberResponse(waiting.getMember());
        this.theme = new ThemeResponse(waiting.getTheme());
        this.time = new ReservationTimeResponse(waiting.getReservationTime());
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

    public ThemeResponse getTheme() {
        return theme;
    }

    public ReservationTimeResponse getTime() {
        return time;
    }
}
