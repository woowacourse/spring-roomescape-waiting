package roomescape.fixture.entity;

import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.waiting.domain.Waiting;
import roomescape.theme.domain.Theme;

public class ReservationWaitingFixture {

    public static Waiting create() {
        return Waiting.builder()
                .reserver(MemberFixture.createUser())
                .reservationDatetime(ReservationDateTimeFixture.create())
                .theme(ThemeFixture.create())
                .build();
    }

    public static Waiting create(Member member, ReservationDateTime dateTime, Theme theme) {
        return Waiting.builder()
                .reserver(member)
                .reservationDatetime(dateTime)
                .theme(theme)
                .build();
    }
} 
