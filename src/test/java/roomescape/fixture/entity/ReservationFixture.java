package roomescape.fixture.entity;

import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.theme.domain.Theme;

public class ReservationFixture {

    public static Reservation create() {
        return Reservation.builder()
                .reserver(MemberFixture.createUser())
                .reservationDateTime(ReservationDateTimeFixture.create())
                .theme(ThemeFixture.create())
                .build();
    }

    public static Reservation create(Member member, ReservationDateTime dateTime, Theme theme) {
        return Reservation.builder()
                .reserver(member)
                .reservationDateTime(dateTime)
                .theme(theme)
                .build();
    }
} 
