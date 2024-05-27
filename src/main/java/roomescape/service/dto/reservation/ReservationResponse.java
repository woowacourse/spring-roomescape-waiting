package roomescape.service.dto.reservation;

import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.WaitingWithRank;
import roomescape.service.dto.member.MemberResponse;
import roomescape.service.dto.theme.ThemeResponse;

public class ReservationResponse {

    private static final String DEFAULT_STATUS = "예약";

    private final long id;
    private final MemberResponse member;
    private final ThemeResponse theme;
    private final String date;
    private final ReservationTimeResponse time;
    private final String status;

    public ReservationResponse(long id, MemberResponse member, ThemeResponse theme, String date,
                               ReservationTimeResponse time, String status) {
        this.id = id;
        this.member = member;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public ReservationResponse(Reservation reservation, String status) {
        this(reservation.getId(),
                new MemberResponse(reservation),
                new ThemeResponse(reservation.getTheme()),
                reservation.getDate().toString(),
                new ReservationTimeResponse(reservation.getTime()),
                status);
    }

    public ReservationResponse(Reservation reservation) {
        this(reservation, DEFAULT_STATUS);
    }

    public ReservationResponse(WaitingWithRank waitingWithRank) {
        this(waitingWithRank.getWaiting().getReservation(), waitingWithRank.formatOrderStatus());
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

    public String getStatus() {
        return status;
    }
}
