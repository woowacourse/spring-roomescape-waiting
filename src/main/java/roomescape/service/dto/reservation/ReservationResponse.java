package roomescape.service.dto.reservation;

import roomescape.domain.reservation.Reservation;
import roomescape.service.dto.member.MemberResponse;
import roomescape.service.dto.theme.ThemeResponse;

public class ReservationResponse {

    private final long id;
    private final MemberResponse member;
    private final ThemeResponse theme;
    private final String date;
    private final ReservationTimeResponse time;
    private final String reservationStatus;

    public ReservationResponse(long id,
                               MemberResponse member,
                               ThemeResponse theme,
                               String date,
                               ReservationTimeResponse time,
                               String reservationStatus) {
        this.id = id;
        this.member = member;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.reservationStatus = reservationStatus;
    }

    public ReservationResponse(Reservation reservation) {
        this(reservation.getId(),
                new MemberResponse(reservation),
                new ThemeResponse(reservation.getTheme()),
                reservation.getDate().toString(),
                new ReservationTimeResponse(reservation.getTime()),
                reservation.getReservationStatus().toString());
    }

    public long getId() {
        return id;
    }

    public MemberResponse getMember() {
        return member;
    }

    public ThemeResponse getTheme() {
        return theme;
    }

    public String getDate() {
        return date;
    }

    public ReservationTimeResponse getTime() {
        return time;
    }

    public String getReservationStatus() {
        return reservationStatus;
    }
}
