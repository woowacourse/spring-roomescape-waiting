package roomescape.fixture;

import static roomescape.fixture.MemberFixture.MEMBER_ARU;
import static roomescape.fixture.ThemeFixture.TEST_THEME;
import static roomescape.fixture.TimeFixture.TEN_AM;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@TestComponent
public class ReservationFixture {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private Clock clock;

    public Reservation saveReservation() {
        ReservationTime time = reservationTimeRepository.save(TEN_AM.create());
        Theme theme = themeRepository.save(TEST_THEME.create());
        Member member = memberRepository.save(MEMBER_ARU.create());
        LocalDateTime createdAt = LocalDateTime.now(clock);
        LocalDate date = LocalDate.of(2024, 1, 1);

        Reservation reservation = new Reservation(member, date, time, theme, createdAt, BookStatus.BOOKED);
        return reservationRepository.save(reservation);
    }
}
