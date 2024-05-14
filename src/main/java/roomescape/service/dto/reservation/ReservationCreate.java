package roomescape.service.dto.reservation;

import java.time.LocalTime;
import roomescape.controller.dto.MemberReservationRequest;
import roomescape.controller.helper.LoginMember;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

public class ReservationCreate {

    private final long memberId;
    private final long themeId;
    private final String date;
    private final long timeId;

    public ReservationCreate(long memberId, long themeId, String date, long timeId) {
        this.memberId = memberId;
        this.themeId = themeId;
        this.date = date;
        this.timeId = timeId;
    }

    public ReservationCreate(LoginMember loginMember, MemberReservationRequest requestDto) {
        this(loginMember.getId(), requestDto.getThemeId(), requestDto.getDate(), requestDto.getTimeId());
    }

    public Reservation toReservation() {
        return new Reservation(
                null,
                new Member(memberId, null, null, null),
                new Theme(themeId, (String) null, null, null),
                new ReservationDate(date),
                new ReservationTime(timeId, (LocalTime) null)
        );
    }
}
