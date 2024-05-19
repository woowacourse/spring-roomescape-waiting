package roomescape.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.fixture.ThemeFixture;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

@SpringBootTest
@Transactional
class JpaThemeRepositoryTest {
    @Autowired
    private JpaThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("전체 테마 조회를 잘 하는지 확인")
    void findAll() {
        Theme theme = themeRepository.save(DEFAULT_THEME);
        List<Theme> allTheme = themeRepository.findAll();

        Assertions.assertThat(allTheme)
                .extracting(Theme::getId)
                .containsExactly(theme.getId());
    }

    @Test
    @DisplayName("인기 순으로 테마를 잘 조회하는지 확인")
    void findAndOrderByPopularity() {
        Theme theme1 = themeRepository.save(ThemeFixture.from("name1"));
        Theme theme2 = themeRepository.save(ThemeFixture.from("name2"));
        Theme theme3 = themeRepository.save(ThemeFixture.from("name3"));

        ReservationTime reservationTime1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(1, 30)));
        ReservationTime reservationTime2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(2, 30)));
        ReservationTime reservationTime3 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(3, 30)));

        LocalDate date = LocalDate.now().plusDays(1);
        Member member = memberRepository.save(DEFAULT_MEMBER);

        reservationRepository.save(new Reservation(member, date, reservationTime2, theme2));
        reservationRepository.save(new Reservation(member, date, reservationTime1, theme2));
        reservationRepository.save(new Reservation(member, date, reservationTime3, theme2));

        reservationRepository.save(new Reservation(member, date, reservationTime1, theme1));
        reservationRepository.save(new Reservation(member, date, reservationTime2, theme1));

        reservationRepository.save(new Reservation(member, date, reservationTime1, theme3));

        List<Theme> result = themeRepository.findAndOrderByPopularity(date, date.plusDays(1), 10);

        Assertions.assertThat(result)
                .extracting(Theme::getId)
                .containsExactly(theme2.getId(), theme1.getId(), theme3.getId());
    }

    @Test
    @DisplayName("테마가 잘 지워지는지 확인")
    void delete() {
        Theme theme = themeRepository.save(DEFAULT_THEME);

        themeRepository.delete(theme.getId());

        Assertions.assertThat(themeRepository.findAll()).isEmpty();
    }
}
