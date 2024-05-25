package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.MEMBER_BRI;
import static roomescape.fixture.ThemeFixture.THEME_1;
import static roomescape.fixture.ThemeFixture.THEME_2;
import static roomescape.fixture.ThemeFixture.THEME_3;
import static roomescape.fixture.TimeFixture.TIME_1;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.test.RepositoryTest;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeName;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.TimeRepository;

class ThemeRepositoryTest extends RepositoryTest {
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private TimeRepository timeRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("인기 테마를 조회할 수 있다.")
    @Test
    void findPopularThemeTest() {
        Theme theme1 = themeRepository.save(THEME_1);
        Theme theme2 = themeRepository.save(THEME_2);
        Theme theme3 = themeRepository.save(THEME_3);

        ReservationTime time = timeRepository.save(TIME_1);
        LocalDate date = LocalDate.now();
        reservationRepository.save(new Reservation(MEMBER_BRI, date.minusDays(1), time, theme2));
        reservationRepository.save(new Reservation(MEMBER_BRI, date.minusDays(2), time, theme2));
        reservationRepository.save(new Reservation(MEMBER_BRI, date.minusDays(3), time, theme2));
        reservationRepository.save(new Reservation(MEMBER_BRI, date.minusDays(4), time, theme3));
        reservationRepository.save(new Reservation(MEMBER_BRI, date.minusDays(5), time, theme3));
        reservationRepository.save(new Reservation(MEMBER_BRI, date.minusDays(6), time, theme1));

        LocalDate startDate = date.minusDays(6);
        LocalDate endDate = date.minusDays(1);

        List<Theme> actual = themeRepository.findThemesSortedByCountOfReservation(startDate, endDate, 2);

        assertThat(actual).containsExactly(theme2, theme3);
    }

    @DisplayName("이름이 일치하는 테마 존재하는 것을 확인할 수 있다.")
    @Test
    void existsByNameTrueTest() {
        themeRepository.save(THEME_1);
        boolean actual = themeRepository.existsByName(new ThemeName(THEME_1.getName()));

        assertThat(actual).isTrue();
    }

    @DisplayName("이름이 일치하는 테마 존재하지 않는 것을 확인할 수 있다.")
    @Test
    void existsByNameFalseTest() {
        boolean actual = themeRepository.existsByName(new ThemeName("없는 테마"));

        assertThat(actual).isFalse();
    }
}
