package roomescape.core.dto.reservation;

import roomescape.core.domain.Reservation;
import roomescape.core.dto.member.MemberResponse;
import roomescape.core.dto.reservationtime.ReservationTimeResponse;
import roomescape.core.dto.theme.ThemeResponse;

public class ReservationResponse {
    private Long id;
    private String date;
    private MemberResponse member;
    private ReservationTimeResponse time;
    private ThemeResponse theme;

    public ReservationResponse(final Reservation reservation) {
        this(reservation.getId(), reservation);
    }

    public ReservationResponse(final Long id, final Reservation reservation) {
        this.id = id;
        this.date = reservation.getDateString();
        this.member = new MemberResponse(reservation.getMember());
        this.time = new ReservationTimeResponse(reservation.getReservationTime());
        this.theme = new ThemeResponse(reservation.getTheme());
    }

    public Long getId() {
        return id;
    }

    public String getDate() {
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
