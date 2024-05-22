package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.domain.reservation.ReservationStatus.CONFIRMED;
import static roomescape.domain.reservation.ReservationStatus.WAITING;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    private final Member member1 = new Member("t11@t1.com", "t1", "재즈", "MEMBER");
    private final Member member2 = new Member("t22@t2.com", "t2", "영이", "MEMBER");

    private final LocalDate date1 = LocalDate.parse("2025-11-30");
    private final LocalDate date2 = LocalDate.parse("2025-12-01");

    private final ReservationTime time1 = new ReservationTime("10:00");
    private final ReservationTime time2 = new ReservationTime("12:00");

    private final Theme theme1 = new Theme("공포", "공포다!", "hi.jpg");
    private final Theme theme2 = new Theme("추리", "추리다!", "hi.jpg");
    private final Theme theme3 = new Theme("액션", "액션다!", "hi.jpg");

    private final Reservation reservation1 = new Reservation(member1, theme2, date1, time1, CONFIRMED);
    private final Reservation reservation2 = new Reservation(member2, theme2, date1, time2, CONFIRMED);
    private final Reservation reservation3 = new Reservation(member2, theme1, date1, time1, WAITING);
    private final Reservation reservation4 = new Reservation(member2, theme2, date2, time2, WAITING);
    private final Reservation reservation5 = new Reservation(member2, theme1, date2, time2, WAITING);
    private final Reservation reservation6 = new Reservation(member2, theme3, date2, time2, WAITING);

    @DisplayName("특정 기간에 테마에 예약이 많은 순서대로 테마 목록을 조회한다. ")
    @Test
    void find_popular_themes() {
        timeRepository.save(time1);
        timeRepository.save(time2);
        themeRepository.save(theme1);
        themeRepository.save(theme2);
        themeRepository.save(theme3);
        memberRepository.save(member1);
        memberRepository.save(member2);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);
        reservationRepository.save(reservation4);
        reservationRepository.save(reservation5);
        reservationRepository.save(reservation6);

        List<Theme> actual = themeRepository.findPopularThemes(
                LocalDate.parse("2025-11-29"),
                LocalDate.parse("2025-12-03")
        );

        assertThat(actual).usingRecursiveComparison().isEqualTo(List.of(theme2, theme1, theme3));
    }
}
