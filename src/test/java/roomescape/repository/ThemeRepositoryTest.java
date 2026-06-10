package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@JdbcTest
@Import({ThemeRepository.class, ReservationRepository.class, ReservationTimeRepository.class})
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeDao;

    @Autowired
    private ReservationRepository reservationDao;

    @Autowired
    private ReservationTimeRepository reservationTimeDao;

    @Test
    void 전체_테마_조회() {
        themeDao.save(new Theme(null, "테마A", "설명", "/a"));
        themeDao.save(new Theme(null, "테마B", "설명", "/b"));

        assertThat(themeDao.findAllThemes()).hasSize(2);
    }

    @Test
    void ID로_테마_조회() {
        Theme saved = themeDao.save(new Theme(null, "우테코 공포물", "설명", "/horror"));

        Theme found = themeDao.findThemeById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("우테코 공포물");
    }

    @Test
    void 인기_테마는_최근_일주일_예약수_내림차순() {
        Theme popular = themeDao.save(new Theme(null, "인기", "설명", "/popular"));
        Theme normal = themeDao.save(new Theme(null, "보통", "설명", "/normal"));
        ReservationTime time = reservationTimeDao.save(new ReservationTime(LocalTime.of(10, 0)));

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        reservationDao.save(new Reservation("a", yesterday, time, popular, LocalDateTime.now()));
        reservationDao.save(new Reservation("b", twoDaysAgo, time, popular, LocalDateTime.now()));
        reservationDao.save(new Reservation("c", yesterday, time, normal, LocalDateTime.now()));

        List<Theme> topThemes = themeDao.findTopThemes(10L);

        assertThat(topThemes).extracting(Theme::getName)
                .containsExactly("인기", "보통");
    }

    @Test
    void 테마_저장() {
        Theme saved = themeDao.save(new Theme(null, "새 테마", "새로 추가된 테마", "/new-theme"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("새 테마");
        assertThat(themeDao.findAllThemes()).hasSize(1);
    }

    @Test
    void 예약_없는_테마_삭제() {
        Theme saved = themeDao.save(new Theme(null, "삭제될 테마", "설명", "/delete"));

        themeDao.delete(saved.getId());

        assertThatThrownBy(() -> themeDao.findThemeById(saved.getId()))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }
}
