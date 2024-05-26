package roomescape.service.reservation.dto;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.service.member.dto.MemberResponse;
import roomescape.service.reservationtime.dto.ReservationTimeResponse;
import roomescape.service.theme.dto.ThemeResponse;

public class ReservationResponse {
    private final Long id;
    private final MemberResponse member;
    private final LocalDate date;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;

    public ReservationResponse(
            Long id, MemberResponse member, LocalDate date, ReservationTimeResponse time, ThemeResponse theme) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public ReservationResponse(Reservation reservation) {
        this(reservation.getId(),
                new MemberResponse(reservation.getMember()),
                reservation.getDate(),
                new ReservationTimeResponse(reservation.getTime()),
                new ThemeResponse(reservation.getTheme())
        );
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
