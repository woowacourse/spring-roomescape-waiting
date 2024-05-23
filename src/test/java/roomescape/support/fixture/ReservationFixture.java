package roomescape.support.fixture;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@TestComponent
@Import({MemberFixture.class, ReservationTimeFixture.class, ThemeFixture.class})
public class ReservationFixture extends Fixture {

    @Autowired
    private MemberFixture memberFixture;

    @Autowired
    private ReservationTimeFixture reservationTimeFixture;

    @Autowired
    private ThemeFixture themeFixture;

    public Reservation save() {
        return save("2024-05-19");
    }

    public Reservation save(String date) {
        Member member = memberFixture.save();
        ReservationTime time = reservationTimeFixture.createAndSave();
        Theme theme = themeFixture.save();
        return save(date, member, time, theme);
    }

    public Reservation save(String date, Member member, ReservationTime time, Theme theme) {
        Reservation reservation = new Reservation(LocalDate.parse(date), member, time, theme);
        em.persist(reservation);
        synchronize();
        return reservation;
    }
}
