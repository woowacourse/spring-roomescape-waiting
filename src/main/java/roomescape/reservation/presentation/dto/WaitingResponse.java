package roomescape.reservation.presentation.dto;

import roomescape.member.presentation.dto.MemberResponse;
import roomescape.reservation.domain.Waiting;

public class WaitingResponse {
    private Long id;
    private MemberResponse member;
    private ThemeResponse theme;
    private ReservationTimeResponse time;

    private WaitingResponse() {
    }

    public WaitingResponse(final Waiting waiting) {
        this.id = waiting.getId();
        this.member = new MemberResponse(waiting.getMember());
        this.theme = new ThemeResponse(waiting.getTheme());
        this.time = new ReservationTimeResponse(waiting.getReservationTime());
    }

    public Long getId() {
        return id;
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
