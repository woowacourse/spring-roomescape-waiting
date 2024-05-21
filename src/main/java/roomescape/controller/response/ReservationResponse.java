package roomescape.controller.response;

import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;

import java.time.LocalDate;

public class ReservationResponse {

    private final long id;
    private final LocalDate date;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;
    private final MemberResponse member;

    private ReservationResponse(long id, LocalDate date, ReservationTimeResponse time, ThemeResponse theme, MemberResponse member) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public static ReservationResponse from(Reservation reservation) {
        ReservationTime time = reservation.getTime();
        Theme theme = reservation.getTheme();
        Member member = reservation.getMember();
        return new ReservationResponse(reservation.getId(), reservation.getDate(),
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
