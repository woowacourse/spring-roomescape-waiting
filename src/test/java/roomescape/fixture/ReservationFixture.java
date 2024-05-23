package roomescape.fixture;

import static roomescape.fixture.MemberFixture.MEMBER_ARU;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationStatusRepository;
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
    private ReservationStatusRepository reservationStatusRepository;

    @Autowired
    private Clock clock;

    public Reservation saveReservation() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(MEMBER_ARU.create());
        Reservation reservation = new Reservation(
                member,
                LocalDate.of(2024, 1, 1),
                time,
                theme,
                LocalDateTime.now(clock)
        );
        return reservationStatusRepository.save(
                new ReservationStatus(reservation, BookStatus.BOOKED)
        ).getReservation();
    }
}
