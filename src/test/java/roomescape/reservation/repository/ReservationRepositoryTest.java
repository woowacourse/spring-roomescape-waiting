package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.MEMBER_BRI;
import static roomescape.fixture.MemberFixture.MEMBER_BROWN;
import static roomescape.fixture.ThemeFixture.THEME_1;
import static roomescape.fixture.ThemeFixture.THEME_2;
import static roomescape.fixture.TimeFixture.TIME_1;
import static roomescape.fixture.TimeFixture.TIME_2;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservation.domain.Reservation;
import roomescape.test.RepositoryTest;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.TimeRepository;

class ReservationRepositoryTest extends RepositoryTest {
    private static final int COUNT_OF_RESERVATION = 4;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private TimeRepository timeRepository;
    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("일부 조건에 따라 예약을 조회할 수 있다.")
    @Test
    void findAllByCondition_whenSomeCondition() {
        // given
        LocalDate date = LocalDate.of(2100, 5, 5);
        ReservationTime time1 = timeRepository.save(TIME_1);
        ReservationTime time2 = timeRepository.save(TIME_2);
        Theme theme1 = themeRepository.save(THEME_1);
        Theme theme2 = themeRepository.save(THEME_2);

        Reservation reservation1 = reservationRepository.save(new Reservation(MEMBER_BRI, date, TIME_1, THEME_1));
        Reservation reservation2 = reservationRepository.save(new Reservation(MEMBER_BRI, date, TIME_2, THEME_1));
        Reservation reservation3 = reservationRepository.save(new Reservation(MEMBER_BRI, date, TIME_1, THEME_2));
        Reservation reservation4 = reservationRepository.save(new Reservation(MEMBER_BRI, date, TIME_2, THEME_2));

        // when
        List<Reservation> actual = reservationRepository.findAllByCondition(null, theme1.getId(), null, null);

        // then
        assertThat(actual).containsExactlyInAnyOrder(reservation1, reservation2);
    }

    @DisplayName("모든 조건에 따라 예약을 조회할 수 있다.")
    @Test
    void findAllByCondition_whenAllCondition() {
        // given
        LocalDate date = LocalDate.of(2100, 5, 5);
        ReservationTime time1 = timeRepository.save(TIME_1);
        ReservationTime time2 = timeRepository.save(TIME_2);
        Theme theme1 = themeRepository.save(THEME_1);
        Theme theme2 = themeRepository.save(THEME_2);

        Reservation reservation1 = reservationRepository.save(new Reservation(MEMBER_BRI, date, TIME_1, THEME_1));
        Reservation reservation2 = reservationRepository.save(new Reservation(MEMBER_BRI, date, TIME_2, THEME_1));
        Reservation reservation3 = reservationRepository.save(new Reservation(MEMBER_BRI, date, TIME_1, THEME_2));
        Reservation reservation4 = reservationRepository.save(new Reservation(MEMBER_BRI, date, TIME_2, THEME_2));


        // when
        List<Reservation> actual = reservationRepository.findAllByCondition(MEMBER_BRI.getId(), theme2.getId(), date, date);

        // then
        assertThat(actual).containsExactlyInAnyOrder(reservation3, reservation4);
    }

    @DisplayName("멤버의 id로 예약을 조회할 수 있다.")
    @Test
    void findByMemberIdTest() {
        // given
        LocalDate date = LocalDate.of(2100, 5, 5);
        ReservationTime time = timeRepository.save(TIME_1);
        Theme theme = themeRepository.save(THEME_1);

        Reservation reservation1 = reservationRepository.save(new Reservation(MEMBER_BRI, date, TIME_1, THEME_1));
        Reservation reservation2 = reservationRepository.save(new Reservation(MEMBER_BRI, date.plusDays(1), TIME_1, THEME_1));
        Reservation reservation3 = reservationRepository.save(new Reservation(MEMBER_BROWN, date.plusDays(2), TIME_1, THEME_1));

        // when
        List<Reservation> actual = reservationRepository.findByMember_id(MEMBER_BRI.getId());

        // then
        assertThat(actual).containsExactlyInAnyOrder(reservation1, reservation2);
    }

    @DisplayName("날짜, 시간, 테마가 일치하는 예약이 존재하는 것을 확인할 수 있다.")
    @Test
    void existsByDateAndTimeIdAndThemeIdTrueTest() {
        // given
        LocalDate date = LocalDate.of(2100, 5, 5);
        ReservationTime time = timeRepository.save(TIME_1);
        Theme theme = themeRepository.save(THEME_1);

        reservationRepository.save(new Reservation(MEMBER_BRI, date, TIME_1, THEME_1));

        boolean actual = reservationRepository.existsByDateAndTime_idAndTheme_id(date, time.getId(), theme.getId());

        assertThat(actual).isTrue();
    }

    @DisplayName("날짜, 시간, 테마가 일치하는 예약이 존재하지 않는 것을 확인할 수 있다.")
    @Test
    void existsByDateAndTimeIdAndThemeIdFalseTest() {
        boolean actual =
                reservationRepository.existsByDateAndTime_idAndTheme_id(LocalDate.now(), 1L, 1L);

        assertThat(actual).isFalse();
    }
}
