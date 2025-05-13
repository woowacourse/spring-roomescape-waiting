package roomescape.reservation.presentation.dto;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;

public class ReservationResponse {
    private Long id;
    private MemberResponse member;
    private ThemeResponse theme;
    private LocalDate date;
    private ReservationTimeResponse time;

    private ReservationResponse() {
    }

    public ReservationResponse(final Reservation reservation) {
        this.id = reservation.getId();
        this.member = new MemberResponse(reservation.getMember());
        this.theme = new ThemeResponse(reservation.getTheme());
        this.date = reservation.getDate().getReservationDate();
        this.time = new ReservationTimeResponse(reservation.getReservationTime());
    }

    public Long getId() {
        return id;
    }

    public MemberResponse getMember() {
        return member;
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

}
