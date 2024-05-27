package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.domain.reservation.ReservationStatus.CONFIRMED;
import static roomescape.domain.reservation.ReservationStatus.WAITING;

import java.time.LocalDate;
import java.util.Set;
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
class ReservationTimeRepositoryTest {

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
    private final ReservationTime time3 = new ReservationTime("14:00");

    private final Theme theme = new Theme("공포", "공포다!", "hi.jpg");

    private final Reservation reservation1 = new Reservation(member1, theme, date1, time1, CONFIRMED);
    private final Reservation reservation2 = new Reservation(member2, theme, date1, time2, CONFIRMED);
    private final Reservation reservation3 = new Reservation(member1, theme, date1, time1, WAITING);

    @DisplayName("특정 날짜와 테마에 대해 예약이 존재하는 시간 목록을 조회해온다.")
    @Test
    void find_reserved_time_by_date_and_theme() {
        timeRepository.save(time1);
        timeRepository.save(time2);
        timeRepository.save(time3);
        themeRepository.save(theme);
        memberRepository.save(member1);
        memberRepository.save(member2);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
        reservationRepository.save(reservation3);

        Set<ReservationTime> actual = reservationTimeRepository.findReservedTimeByDateAndTheme(date1, 1L);

        assertThat(actual).usingRecursiveComparison().isEqualTo(Set.of(time1, time2));
    }
}
