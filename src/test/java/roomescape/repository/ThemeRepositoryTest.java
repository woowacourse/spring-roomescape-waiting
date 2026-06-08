package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@JdbcTest
@Import({ThemeRepository.class, ReservationRepository.class, ReservationTimeRepository.class})
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    void ID로_테마_조회() {
        Theme saved = themeRepository.save(new Theme("테스트 테마", "설명", "/horror"));
        Optional<Theme> theme = themeRepository.findThemeById(saved.getId());

        assertThat(theme)
                .map(Theme::getId)
                .hasValue(saved.getId());

        assertThat(theme)
                .map(Theme::getName)
                .hasValue("테스트 테마");
    }

    @Test
    void 인기_테마_상위_3개_조회() {
        Theme theme1 = themeRepository.save(new Theme("인기테마1", "설명", "url"));
        Theme theme2 = themeRepository.save(new Theme("인기테마2", "설명", "url"));
        Theme theme3 = themeRepository.save(new Theme("인기테마3", "설명", "url"));
        ReservationTime time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(1, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(2, 0)));
        ReservationTime time3 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(3, 0)));

        reservationRepository.save(
                new Reservation("A", LocalDate.now().minusDays(1), time1, theme1, ReservationStatus.CONFIRMED));
        reservationRepository.save(
                new Reservation("B", LocalDate.now().minusDays(1), time2, theme1, ReservationStatus.CONFIRMED));
        reservationRepository.save(
                new Reservation("C", LocalDate.now().minusDays(1), time3, theme1, ReservationStatus.CONFIRMED));

        reservationRepository.save(
                new Reservation("D", LocalDate.now().minusDays(1), time1, theme2, ReservationStatus.CONFIRMED));
        reservationRepository.save(
                new Reservation("E", LocalDate.now().minusDays(1), time2, theme2, ReservationStatus.CONFIRMED));

        reservationRepository.save(
                new Reservation("F", LocalDate.now().minusDays(1), time1, theme3, ReservationStatus.CONFIRMED));

        List<Theme> topThemes = themeRepository.findTopThemes(3L);

        assertThat(topThemes).hasSize(3);
        assertThat(topThemes.get(0).getName()).isEqualTo("인기테마1");
        assertThat(topThemes.get(1).getName()).isEqualTo("인기테마2");
        assertThat(topThemes.get(2).getName()).isEqualTo("인기테마3");
    }

    @Test
    void 테마_저장() {
        Theme newTheme = new Theme("새 테마", "설명", "/new-theme");

        Theme saved = themeRepository.save(newTheme);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("새 테마");
    }

    @Test
    void 예약_없는_테마_삭제() {
        Theme saved = themeRepository.save(new Theme("삭제 테마", "설명", "url"));

        themeRepository.delete(saved.getId());

        assertThat(themeRepository.findThemeById(saved.getId())).isEmpty();
    }
}
