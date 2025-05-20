package roomescape.fixture.entity;

import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.theme.domain.Theme;

public class ReservationWaitingFixture {

    public static ReservationWaiting create() {
        return ReservationWaiting.builder()
                .reserver(MemberFixture.createUser())
                .reservationDatetime(ReservationDateTimeFixture.create())
                .theme(ThemeFixture.create())
                .build();
    }

    public static ReservationWaiting create(Member member, ReservationDateTime dateTime, Theme theme) {
        return ReservationWaiting.builder()
                .reserver(member)
                .reservationDatetime(dateTime)
                .theme(theme)
                .build();
    }
} 
